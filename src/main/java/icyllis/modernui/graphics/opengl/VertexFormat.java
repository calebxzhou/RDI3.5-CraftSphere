/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.graphics.opengl;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;

import static icyllis.modernui.graphics.opengl.GLCore.*;

/**
 * Represents a set of vertex attribute specifications used with a vertex shader.
 */
public class VertexFormat {

    private final VertexAttrib[][] mAttributes;

    private int mVertexArray = INVALID_ID;

    /**
     * Creates a vertex format.
     * <p>
     * Attribute index depends on the order of <code>attribs</code> and bindings,
     * you must use explicit attribute layout for the base attribute index,
     * (layout = index). Each attribute takes up <code>getCount()</code> locations
     * in total. Binding points are given priority to assign, must be sequential.
     *
     * @param attribs all attribs
     */
    public VertexFormat(@Nonnull VertexAttrib... attribs) {
        final int len = attribs.length;
        if (len == 0) {
            throw new IllegalArgumentException("No attribs");
        }
        // sequential generator
        Arrays.sort(attribs, Comparator.comparingInt(VertexAttrib::getBinding));
        mAttributes = new VertexAttrib[attribs[len - 1].getBinding() + 1][];
        int pos = 0, binding = 0;
        for (int i = 0; i <= len; i++) {
            VertexAttrib a = i < len ? attribs[i] : null;
            if (a != null && binding == a.getBinding()) {
                continue;
            }
            VertexAttrib[] arr = new VertexAttrib[i - pos];
            for (int j = pos, k = 0; j < i; j++, k++) {
                arr[k] = attribs[j];
            }
            mAttributes[binding] = arr;
            if (a == null) {
                break;
            }
            pos = i;
            for (int j = binding + 1; j < a.getBinding(); j++) {
                mAttributes[j] = new VertexAttrib[0];
            }
            binding = a.getBinding();
        }
    }

    /**
     * @return the shared vertex array object.
     */
    public int getVertexArray() {
        if (mVertexArray == INVALID_ID) {
            setFormat(mVertexArray = glCreateVertexArrays());
        }
        return mVertexArray;
    }

    /**
     * Configure a vertex array object sequentially.
     *
     * @param array a custom vertex array
     */
    public void setFormat(int array) {
        int location = 0;
        for (var attribs : mAttributes) {
            // relative offset in binding point
            int offset = 0;
            for (var a : attribs) {
                offset = a.setFormat(array, location, offset);
                location += a.getCount();
            }
        }
    }

    /**
     * @return the max binding point of this VertexFormat
     */
    public int getMaxBinding() {
        return mAttributes.length - 1;
    }

    /**
     * Set the buffer that stores the attribute data.
     * <p>
     * The stride, the distance to the next vertex/instance data, in bytes, will
     * be {@link #getBindingSize(int)}
     *
     * @param binding the binding index
     * @param buffer  the vertex buffer object
     * @param offset  first vertex/instance data to the head of the buffer, in bytes
     */
    public void setVertexBuffer(int binding, @Nonnull GLBuffer buffer, int offset) {
        glVertexArrayVertexBuffer(getVertexArray(), binding, buffer.get(), offset, getBindingSize(binding));
        /*ModernUI.LOGGER.info("Bind Vertex Buffer: {VAO={}, bind={}, buffer={}, offset={}, stride={}}",
                getVertexArray(), binding, buffer, offset, getBindingSize(binding));*/
    }

    /**
     * Set instanced array.
     *
     * @param binding the binding index
     * @param divisor instance divisor
     */
    public void setBindingDivisor(int binding, int divisor) {
        glVertexArrayBindingDivisor(getVertexArray(), binding, divisor);
    }

    /**
     * Set element buffer (index buffer).
     *
     * @param buffer the element buffer object
     */
    public void setElementBuffer(@Nonnull GLBuffer buffer) {
        glVertexArrayElementBuffer(getVertexArray(), buffer.get());
    }

    /**
     * Get the sum of the sizes of all attributes to the given binding point.
     *
     * @param binding the binding index
     * @return total size in bytes
     */
    public int getBindingSize(int binding) {
        int size = 0;
        for (var a : mAttributes[binding]) {
            size += a.getTotalSize();
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.deepEquals(mAttributes, ((VertexFormat) o).mAttributes);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(mAttributes);
    }
}

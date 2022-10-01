#version 450 core

layout(std140, binding = 0) uniform MatrixBlock {
    mat4 u_Projection;
    mat4 u_ModelView;
    vec4 u_Color;
};

layout(location = 0) in vec2 a_Pos;
layout(location = 1) in vec2 a_UV;

flat out vec4 f_Color;
smooth out vec2 f_TexCoord;

void main() {
    f_Color = u_Color;
    f_TexCoord = a_UV;

    gl_Position = u_Projection * u_ModelView * vec4(a_Pos, 0.0, 1.0);
}
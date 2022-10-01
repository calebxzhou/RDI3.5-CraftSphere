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

package icyllis.modernui.transition;

import icyllis.modernui.animation.Animator;
import icyllis.modernui.animation.AnimatorListener;
import icyllis.modernui.animation.TimeInterpolator;
import icyllis.modernui.math.Rect;
import icyllis.modernui.util.ArrayMap;
import icyllis.modernui.util.LongSparseArray;
import icyllis.modernui.util.SparseArray;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.ListView;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.intellij.lang.annotations.MagicConstant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A Transition holds information about animations that will be run on its
 * targets during a scene change. Subclasses of this abstract class may
 * choreograph several child transitions ({@link TransitionSet} or they may
 * perform custom animations themselves. Any Transition has two main jobs:
 * (1) capture property values, and (2) play animations based on changes to
 * captured property values. A custom transition knows what property values
 * on View objects are of interest to it, and also knows how to animate
 * changes to those values. For example, the {@link Fade} transition tracks
 * changes to visibility-related properties and is able to construct and run
 * animations that fade items in or out based on changes to those properties.
 */
public abstract class Transition implements Cloneable {

    /**
     * With {@link #setMatchOrder(int...)}, chooses to match by View instance.
     */
    public static final int MATCH_INSTANCE = 0x1;

    private static final int MATCH_FIRST = MATCH_INSTANCE;

    /**
     * With {@link #setMatchOrder(int...)}, chooses to match by
     * {@link View#getTransitionName()}. Null names will not be matched.
     */
    public static final int MATCH_NAME = 0x2;

    /**
     * With {@link #setMatchOrder(int...)}, chooses to match by
     * {@link View#getId()}. Negative IDs will not be matched.
     */
    public static final int MATCH_ID = 0x3;

    /**
     * With {@link #setMatchOrder(int...)}, chooses to match by the {@link icyllis.modernui.widget.Adapter}
     * item id. When {@link icyllis.modernui.widget.Adapter#hasStableIds()} returns false, no match
     * will be made for items.
     */
    public static final int MATCH_ITEM_ID = 0x4;

    private static final int MATCH_LAST = MATCH_ITEM_ID;

    @MagicConstant(intValues = {MATCH_INSTANCE, MATCH_NAME, MATCH_ID, MATCH_ITEM_ID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MatchOrder {
    }

    private static final int[] DEFAULT_MATCH_ORDER = {
            MATCH_NAME,
            MATCH_INSTANCE,
            MATCH_ID,
            MATCH_ITEM_ID,
    };

    private long mStartDelay = -1;
    long mDuration = -1;
    private TimeInterpolator mInterpolator;
    IntArrayList mTargetIds = new IntArrayList();
    ArrayList<View> mTargets = new ArrayList<>();
    private ArrayList<String> mTargetNames;
    private ArrayList<Class<?>> mTargetTypes;
    private IntArrayList mTargetIdExcludes;
    private ArrayList<View> mTargetExcludes;
    private ArrayList<Class<?>> mTargetTypeExcludes;
    private ArrayList<String> mTargetNameExcludes;
    private IntArrayList mTargetIdChildExcludes;
    private ArrayList<View> mTargetChildExcludes;
    private ArrayList<Class<?>> mTargetTypeChildExcludes;
    private TransitionValuesMaps mStartValues = new TransitionValuesMaps();
    private TransitionValuesMaps mEndValues = new TransitionValuesMaps();
    TransitionSet mParent;
    private int[] mMatchOrder = DEFAULT_MATCH_ORDER;
    private ArrayList<TransitionValues> mStartValuesList; // only valid after playTransition starts
    private ArrayList<TransitionValues> mEndValuesList; // only valid after playTransitions starts

    // Per-animator information used for later canceling when future transitions overlap
    private static final ThreadLocal<ArrayMap<Animator, AnimationInfo>> sRunningAnimators =
            ThreadLocal.withInitial(ArrayMap::new);

    // Whether removing views from their parent is possible. This is only for views
    // in the start scene, which are no longer in the view hierarchy. This property
    // is determined by whether the previous Scene was created from a layout
    // resource, and thus the views from the exited scene are going away anyway
    // and can be removed as necessary to achieve a particular effect, such as
    // removing them from parents to add them to overlays.
    boolean mCanRemoveViews = false;

    // Track all animators in use in case the transition gets canceled and needs to
    // cancel running animators
    private final ArrayList<Animator> mCurrentAnimators = new ArrayList<>();

    // Number of per-target instances of this Transition currently running. This count is
    // determined by calls to start() and end()
    private int mNumInstances = 0;

    // Whether this transition is currently paused, due to a call to pause()
    private boolean mPaused = false;

    // Whether this transition has ended. Used to avoid pause/resume on transitions
    // that have completed
    private boolean mEnded = false;

    // The set of listeners to be sent transition lifecycle events.
    private CopyOnWriteArrayList<TransitionListener> mListeners;

    // The set of animators collected from calls to createAnimator(),
    // to be run in runAnimators()
    private ArrayList<Animator> mAnimators = new ArrayList<>();

    // The function for calculating the Animation start delay.
    TransitionPropagation mPropagation;

    // The rectangular region for Transitions like Explode and TransitionPropagations
    // like CircularPropagation
    private EpicenterCallback mEpicenterCallback;

    /**
     * Constructs a Transition object with no target objects. A transition with
     * no targets defaults to running on all target objects in the scene hierarchy
     * (if the transition is not contained in a TransitionSet), or all target
     * objects passed down from its parent (if it is in a TransitionSet).
     */
    public Transition() {
    }

    /**
     * Sets the duration of this transition. By default, there is no duration
     * (indicated by a negative number), which means that the Animator created by
     * the transition will have its own specified duration. If the duration of a
     * Transition is set, that duration will override the Animator duration.
     *
     * @param duration The length of the animation, in milliseconds.
     * @return This transition object.
     */
    @Nonnull
    public Transition setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Returns the duration set on this transition. If no duration has been set,
     * the returned value will be negative, indicating that resulting animators will
     * retain their own durations.
     *
     * @return The duration set on this transition, in milliseconds, if one has been
     * set, otherwise returns a negative number.
     */
    public long getDuration() {
        return mDuration;
    }

    /**
     * Sets the startDelay of this transition. By default, there is no delay
     * (indicated by a negative number), which means that the Animator created by
     * the transition will have its own specified startDelay. If the delay of a
     * Transition is set, that delay will override the Animator delay.
     *
     * @param startDelay The length of the delay, in milliseconds.
     * @return This transition object.
     */
    @Nonnull
    public Transition setStartDelay(long startDelay) {
        mStartDelay = startDelay;
        return this;
    }

    /**
     * Returns the startDelay set on this transition. If no startDelay has been set,
     * the returned value will be negative, indicating that resulting animators will
     * retain their own startDelays.
     *
     * @return The startDelay set on this transition, in milliseconds, if one has
     * been set, otherwise returns a negative number.
     */
    public long getStartDelay() {
        return mStartDelay;
    }

    /**
     * Sets the interpolator of this transition. By default, the interpolator
     * is null, which means that the Animator created by the transition
     * will have its own specified interpolator. If the interpolator of a
     * Transition is set, that interpolator will override the Animator interpolator.
     *
     * @param interpolator The time interpolator used by the transition
     * @return This transition object.
     */
    @Nonnull
    public Transition setInterpolator(@Nullable TimeInterpolator interpolator) {
        mInterpolator = interpolator;
        return this;
    }

    /**
     * Returns the interpolator set on this transition. If no interpolator has been set,
     * the returned value will be null, indicating that resulting animators will
     * retain their own interpolators.
     *
     * @return The interpolator set on this transition, if one has been set, otherwise
     * returns null.
     */
    @Nullable
    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }

    /**
     * Returns the set of property names used stored in the {@link TransitionValues}
     * object passed into {@link #captureStartValues(TransitionValues)} that
     * this transition cares about for the purposes of canceling overlapping animations.
     * When any transition is started on a given scene root, all transitions
     * currently running on that same scene root are checked to see whether the
     * properties on which they based their animations agree with the end values of
     * the same properties in the new transition. If the end values are not equal,
     * then the old animation is canceled since the new transition will start a new
     * animation to these new values. If the values are equal, the old animation is
     * allowed to continue and no new animation is started for that transition.
     *
     * <p>A transition does not need to override this method. However, not doing so
     * will mean that the cancellation logic outlined in the previous paragraph
     * will be skipped for that transition, possibly leading to artifacts as
     * old transitions and new transitions on the same targets run in parallel,
     * animating views toward potentially different end values.</p>
     *
     * @return An array of property names as described in the class documentation for
     * {@link TransitionValues}. The default implementation returns <code>null</code>.
     */
    @Nullable
    public String[] getTransitionProperties() {
        return null;
    }

    /**
     * This method creates an animation that will be run for this transition
     * given the information in the startValues and endValues structures captured
     * earlier for the start and end scenes. Subclasses of Transition should override
     * this method. The method should only be called by the transition system; it is
     * not intended to be called from external classes.
     *
     * <p>This method is called by the transition's parent (all the way up to the
     * topmost Transition in the hierarchy) with the sceneRoot and start/end
     * values that the transition may need to set up initial target values
     * and construct an appropriate animation. For example, if an overall
     * Transition is a {@link TransitionSet} consisting of several
     * child transitions in sequence, then some of the child transitions may
     * want to set initial values on target views prior to the overall
     * Transition commencing, to put them in an appropriate state for the
     * delay between that start and the child Transition start time. For
     * example, a transition that fades an item in may wish to set the starting
     * alpha value to 0, to avoid it blinking in prior to the transition
     * actually starting the animation. This is necessary because the scene
     * change that triggers the Transition will automatically set the end-scene
     * on all target views, so a Transition that wants to animate from a
     * different value should set that value prior to returning from this method.</p>
     *
     * <p>Additionally, a Transition can perform logic to determine whether
     * the transition needs to run on the given target and start/end values.
     * For example, a transition that resizes objects on the screen may wish
     * to avoid running for views which are not present in either the start
     * or end scenes.</p>
     *
     * <p>If there is an animator created and returned from this method, the
     * transition mechanism will apply any applicable duration, startDelay,
     * and interpolator to that animation and start it. A return value of
     * <code>null</code> indicates that no animation should run. The default
     * implementation returns null.</p>
     *
     * <p>The method is called for every applicable target object, which is
     * stored in the {@link TransitionValues#view} field.</p>
     *
     * @param sceneRoot   The root of the transition hierarchy.
     * @param startValues The values for a specific target in the start scene.
     * @param endValues   The values for the target in the end scene.
     * @return An Animator to be started at the appropriate time in the
     * overall transition for this scene change. A null value means no animation
     * should be run.
     */
    @Nullable
    public Animator createAnimator(@Nonnull ViewGroup sceneRoot, @Nullable TransitionValues startValues,
                                   @Nullable TransitionValues endValues) {
        return null;
    }

    /**
     * Sets the order in which Transition matches View start and end values.
     * <p>
     * The default behavior is to match first by {@link View#getTransitionName()},
     * then by View instance, then by {@link View#getId()} and finally
     * by its item ID if it is in a direct child of ListView. The caller can
     * choose to have only some or all of the values of {@link #MATCH_INSTANCE},
     * {@link #MATCH_NAME}, {@link #MATCH_ITEM_ID}, and {@link #MATCH_ID}. Only
     * the match algorithms supplied will be used to determine whether Views are the
     * the same in both the start and end Scene. Views that do not match will be considered
     * as entering or leaving the Scene.
     * </p>
     *
     * @param matches A list of zero or more of {@link #MATCH_INSTANCE},
     *                {@link #MATCH_NAME}, {@link #MATCH_ITEM_ID}, and {@link #MATCH_ID}.
     *                If none are provided, then the default match order will be set.
     */
    public void setMatchOrder(@MatchOrder int... matches) {
        if (matches == null || matches.length == 0) {
            mMatchOrder = DEFAULT_MATCH_ORDER;
        } else {
            for (int i = 0; i < matches.length; i++) {
                int match = matches[i];
                if (!isValidMatch(match)) {
                    throw new IllegalArgumentException("matches contains invalid value");
                }
                if (alreadyContains(matches, i)) {
                    throw new IllegalArgumentException("matches contains a duplicate value");
                }
            }
            mMatchOrder = matches.clone();
        }
    }

    private static boolean isValidMatch(int match) {
        return (match >= MATCH_FIRST && match <= MATCH_LAST);
    }

    private static boolean alreadyContains(@Nonnull int[] array, int searchIndex) {
        int value = array[searchIndex];
        for (int i = 0; i < searchIndex; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Match start/end values by View instance. Adds matched values to mStartValuesList
     * and mEndValuesList and removes them from unmatchedStart and unmatchedEnd.
     */
    private void matchInstances(@Nonnull ArrayMap<View, TransitionValues> unmatchedStart,
                                @Nonnull ArrayMap<View, TransitionValues> unmatchedEnd) {
        for (int i = unmatchedStart.size() - 1; i >= 0; i--) {
            View view = unmatchedStart.keyAt(i);
            if (view != null && isValidTarget(view)) {
                TransitionValues end = unmatchedEnd.remove(view);
                if (end != null && isValidTarget(end.view)) {
                    TransitionValues start = unmatchedStart.removeAt(i);
                    mStartValuesList.add(start);
                    mEndValuesList.add(end);
                }
            }
        }
    }

    /**
     * Match start/end values by Adapter item ID. Adds matched values to mStartValuesList
     * and mEndValuesList and removes them from unmatchedStart and unmatchedEnd, using
     * startItemIds and endItemIds as a guide for which Views have unique item IDs.
     */
    private void matchItemIds(@Nonnull ArrayMap<View, TransitionValues> unmatchedStart,
                              @Nonnull ArrayMap<View, TransitionValues> unmatchedEnd,
                              @Nonnull LongSparseArray<View> startItemIds,
                              @Nonnull LongSparseArray<View> endItemIds) {
        final int numStartIds = startItemIds.size();
        for (int i = 0; i < numStartIds; i++) {
            View startView = startItemIds.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = endItemIds.get(startItemIds.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = unmatchedStart.get(startView);
                    TransitionValues endValues = unmatchedEnd.get(endView);
                    if (startValues != null && endValues != null) {
                        mStartValuesList.add(startValues);
                        mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    /**
     * Match start/end values by Adapter view ID. Adds matched values to mStartValuesList
     * and mEndValuesList and removes them from unmatchedStart and unmatchedEnd, using
     * startIds and endIds as a guide for which Views have unique IDs.
     */
    private void matchIds(@Nonnull ArrayMap<View, TransitionValues> unmatchedStart,
                          @Nonnull ArrayMap<View, TransitionValues> unmatchedEnd,
                          @Nonnull SparseArray<View> startIds,
                          @Nonnull SparseArray<View> endIds) {
        final int numStartIds = startIds.size();
        for (int i = 0; i < numStartIds; i++) {
            View startView = startIds.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = endIds.get(startIds.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = unmatchedStart.get(startView);
                    TransitionValues endValues = unmatchedEnd.get(endView);
                    if (startValues != null && endValues != null) {
                        mStartValuesList.add(startValues);
                        mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    /**
     * Match start/end values by Adapter transitionName. Adds matched values to mStartValuesList
     * and mEndValuesList and removes them from unmatchedStart and unmatchedEnd, using
     * startNames and endNames as a guide for which Views have unique transitionNames.
     */
    private void matchNames(@Nonnull ArrayMap<View, TransitionValues> unmatchedStart,
                            @Nonnull ArrayMap<View, TransitionValues> unmatchedEnd,
                            @Nonnull ArrayMap<String, View> startNames,
                            @Nonnull ArrayMap<String, View> endNames) {
        final int numStartNames = startNames.size();
        for (int i = 0; i < numStartNames; i++) {
            View startView = startNames.valueAt(i);
            if (startView != null && isValidTarget(startView)) {
                View endView = endNames.get(startNames.keyAt(i));
                if (endView != null && isValidTarget(endView)) {
                    TransitionValues startValues = unmatchedStart.get(startView);
                    TransitionValues endValues = unmatchedEnd.get(endView);
                    if (startValues != null && endValues != null) {
                        mStartValuesList.add(startValues);
                        mEndValuesList.add(endValues);
                        unmatchedStart.remove(startView);
                        unmatchedEnd.remove(endView);
                    }
                }
            }
        }
    }

    /**
     * Adds all values from unmatchedStart and unmatchedEnd to mStartValuesList and mEndValuesList,
     * assuming that there is no match between values in the list.
     */
    private void addUnmatched(@Nonnull ArrayMap<View, TransitionValues> unmatchedStart,
                              @Nonnull ArrayMap<View, TransitionValues> unmatchedEnd) {
        // Views that only exist in the start Scene
        for (int i = 0; i < unmatchedStart.size(); i++) {
            final TransitionValues start = unmatchedStart.valueAt(i);
            if (isValidTarget(start.view)) {
                mStartValuesList.add(start);
                mEndValuesList.add(null);
            }
        }

        // Views that only exist in the end Scene
        for (int i = 0; i < unmatchedEnd.size(); i++) {
            final TransitionValues end = unmatchedEnd.valueAt(i);
            if (isValidTarget(end.view)) {
                mEndValuesList.add(end);
                mStartValuesList.add(null);
            }
        }
    }

    private void matchStartAndEnd(@Nonnull TransitionValuesMaps startValues,
                                  @Nonnull TransitionValuesMaps endValues) {
        ArrayMap<View, TransitionValues> unmatchedStart = new ArrayMap<>(startValues.mViewValues);
        ArrayMap<View, TransitionValues> unmatchedEnd = new ArrayMap<>(endValues.mViewValues);

        for (int j : mMatchOrder) {
            switch (j) {
                case MATCH_INSTANCE -> matchInstances(unmatchedStart, unmatchedEnd);
                case MATCH_NAME -> matchNames(unmatchedStart, unmatchedEnd,
                        startValues.mNameValues, endValues.mNameValues);
                case MATCH_ID -> matchIds(unmatchedStart, unmatchedEnd,
                        startValues.mIdValues, endValues.mIdValues);
                case MATCH_ITEM_ID -> matchItemIds(unmatchedStart, unmatchedEnd,
                        startValues.mItemIdValues, endValues.mItemIdValues);
            }
        }
        addUnmatched(unmatchedStart, unmatchedEnd);
    }

    /**
     * This method, essentially a wrapper around all calls to createAnimator for all
     * possible target views, is called with the entire set of start/end
     * values. The implementation in Transition iterates through these lists
     * and calls {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}
     * with each set of start/end values on this transition. The
     * TransitionSet subclass overrides this method and delegates it to
     * each of its children in succession.
     */
    protected void createAnimators(@Nonnull ViewGroup sceneRoot,
                                   @Nonnull TransitionValuesMaps startValues,
                                   @Nonnull TransitionValuesMaps endValues,
                                   @Nonnull ArrayList<TransitionValues> startValuesList,
                                   @Nonnull ArrayList<TransitionValues> endValuesList) {
        final ArrayMap<Animator, AnimationInfo> runningAnimators = sRunningAnimators.get();
        long minStartDelay = Long.MAX_VALUE;
        final Int2LongArrayMap startDelays = new Int2LongArrayMap();
        final int startValuesListCount = startValuesList.size();
        for (int i = 0; i < startValuesListCount; ++i) {
            TransitionValues start = startValuesList.get(i);
            TransitionValues end = endValuesList.get(i);
            if (start != null && !start.mTargetedTransitions.contains(this)) {
                start = null;
            }
            if (end != null && !end.mTargetedTransitions.contains(this)) {
                end = null;
            }
            if (start == null && end == null) {
                continue;
            }
            // Only bother trying to animate with values that differ between start/end
            boolean isChanged = start == null || end == null || isTransitionRequired(start, end);
            if (isChanged) {
                // TODO: what to do about targetIds and itemIds?
                Animator animator = createAnimator(sceneRoot, start, end);
                if (animator != null) {
                    // Save animation info for future cancellation purposes
                    View view;
                    TransitionValues infoValues = null;
                    if (end != null) {
                        view = end.view;
                        String[] properties = getTransitionProperties();
                        if (properties != null && properties.length > 0) {
                            infoValues = new TransitionValues(view);
                            TransitionValues newValues = endValues.mViewValues.get(view);
                            if (newValues != null) {
                                for (String property : properties) {
                                    infoValues.values.put(property, newValues.values.get(property));
                                }
                            }
                            int numExistingAnimators = runningAnimators.size();
                            for (int j = 0; j < numExistingAnimators; ++j) {
                                Animator anim = runningAnimators.keyAt(j);
                                AnimationInfo info = runningAnimators.get(anim);
                                if (info.values != null && info.view == view
                                        && info.name.equals(getName())) {
                                    if (info.values.equals(infoValues)) {
                                        // Favor the old animator
                                        animator = null;
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        view = start.view;
                    }
                    if (animator != null) {
                        if (mPropagation != null) {
                            long delay = mPropagation.getStartDelay(sceneRoot, this, start, end);
                            startDelays.put(mAnimators.size(), (int) delay);
                            minStartDelay = Math.min(delay, minStartDelay);
                        }
                        AnimationInfo info = new AnimationInfo(view, getName(), this, infoValues);
                        runningAnimators.put(animator, info);
                        mAnimators.add(animator);
                    }
                }
            }
        }
        if (startDelays.size() > 0) {
            for (ObjectIterator<Int2LongMap.Entry> it = startDelays.int2LongEntrySet().fastIterator(); it.hasNext(); ) {
                Int2LongMap.Entry e = it.next();
                int index = e.getIntKey();
                Animator animator = mAnimators.get(index);
                long delay = e.getLongValue() - minStartDelay + animator.getStartDelay();
                animator.setStartDelay(delay);
            }
        }
    }

    /**
     * Internal utility method for checking whether a given view/id
     * is valid for this transition, where "valid" means that either
     * the Transition has no target/targetId list (the default, in which
     * cause the transition should act on all views in the hierarchy), or
     * the given view is in the target list or the view id is in the
     * targetId list. If the target parameter is null, then the target list
     * is not checked (this is in the case of ListView items, where the
     * views are ignored and only the ids are used).
     */
    boolean isValidTarget(@Nonnull View target) {
        int targetId = target.getId();
        if (mTargetIdExcludes != null && mTargetIdExcludes.contains(targetId)) {
            return false;
        }
        if (mTargetExcludes != null && mTargetExcludes.contains(target)) {
            return false;
        }
        if (mTargetTypeExcludes != null) {
            for (Class<?> type : mTargetTypeExcludes) {
                if (type.isInstance(target)) {
                    return false;
                }
            }
        }
        if (mTargetNameExcludes != null && target.getTransitionName() != null) {
            if (mTargetNameExcludes.contains(target.getTransitionName())) {
                return false;
            }
        }
        if (mTargetIds.size() == 0 && mTargets.size() == 0 &&
                (mTargetTypes == null || mTargetTypes.isEmpty()) &&
                (mTargetNames == null || mTargetNames.isEmpty())) {
            return true;
        }
        if (mTargetIds.contains(targetId) || mTargets.contains(target)) {
            return true;
        }
        if (mTargetNames != null && mTargetNames.contains(target.getTransitionName())) {
            return true;
        }
        if (mTargetTypes != null) {
            for (Class<?> targetType : mTargetTypes) {
                if (targetType.isInstance(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This is called internally once all animations have been set up by the
     * transition hierarchy.
     */
    protected void runAnimators() {
        start();
        ArrayMap<Animator, AnimationInfo> runningAnimators = sRunningAnimators.get();
        // Now start every Animator that was previously created for this transition
        for (Animator anim : mAnimators) {
            if (runningAnimators.containsKey(anim)) {
                start();
                runAnimator(anim, runningAnimators);
            }
        }
        mAnimators.clear();
        end();
    }

    private void runAnimator(@Nullable Animator animator,
                             final ArrayMap<Animator, AnimationInfo> runningAnimators) {
        if (animator != null) {
            // TODO: could be a single listener instance for all of them since it uses the param
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(@Nonnull Animator animation) {
                    mCurrentAnimators.add(animation);
                }

                @Override
                public void onAnimationEnd(@Nonnull Animator animation) {
                    runningAnimators.remove(animation);
                    mCurrentAnimators.remove(animation);
                }
            });
            animate(animator);
        }
    }

    /**
     * Captures the values in the start scene for the properties that this
     * transition monitors. These values are then passed as the startValues
     * structure in a later call to
     * {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}.
     * The main concern for an implementation is what the
     * properties are that the transition cares about and what the values are
     * for all of those properties. The start and end values will be compared
     * later during the
     * {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}
     * method to determine what, if any, animations, should be run.
     *
     * <p>Subclasses must implement this method. The method should only be called by the
     * transition system; it is not intended to be called from external classes.</p>
     *
     * @param transitionValues The holder for any values that the Transition
     *                         wishes to store. Values are stored in the <code>values</code> field
     *                         of this TransitionValues object and are keyed from
     *                         a String value. For example, to store a view's rotation value,
     *                         a transition might call
     *                         <code>transitionValues.values.put("appname:transitionname:rotation",
     *                         view.getRotation())</code>. The target view will already be stored
     *                         in
     *                         the transitionValues structure when this method is called.
     * @see #captureEndValues(TransitionValues)
     * @see #createAnimator(ViewGroup, TransitionValues, TransitionValues)
     */
    public abstract void captureStartValues(@Nonnull TransitionValues transitionValues);

    /**
     * Captures the values in the end scene for the properties that this
     * transition monitors. These values are then passed as the endValues
     * structure in a later call to
     * {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}.
     * The main concern for an implementation is what the
     * properties are that the transition cares about and what the values are
     * for all of those properties. The start and end values will be compared
     * later during the
     * {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}
     * method to determine what, if any, animations, should be run.
     *
     * <p>Subclasses must implement this method. The method should only be called by the
     * transition system; it is not intended to be called from external classes.</p>
     *
     * @param transitionValues The holder for any values that the Transition
     *                         wishes to store. Values are stored in the <code>values</code> field
     *                         of this TransitionValues object and are keyed from
     *                         a String value. For example, to store a view's rotation value,
     *                         a transition might call
     *                         <code>transitionValues.values.put("appname:transitionname:rotation",
     *                         view.getRotation())</code>. The target view will already be stored
     *                         in
     *                         the transitionValues structure when this method is called.
     * @see #captureStartValues(TransitionValues)
     * @see #createAnimator(ViewGroup, TransitionValues, TransitionValues)
     */
    public abstract void captureEndValues(@Nonnull TransitionValues transitionValues);

    /**
     * Sets the target view instances that this Transition is interested in
     * animating. By default, there are no targets, and a Transition will
     * listen for changes on every view in the hierarchy below the sceneRoot
     * of the Scene being transitioned into. Setting targets constrains
     * the Transition to only listen for, and act on, these views.
     * All other views will be ignored.
     *
     * <p>The target list is like the {@link #addTarget(int) targetId}
     * list except this list specifies the actual View instances, not the ids
     * of the views. This is an important distinction when scene changes involve
     * view hierarchies which have been inflated separately; different views may
     * share the same id but not actually be the same instance. If the transition
     * should treat those views as the same, then {@link #addTarget(int)} should be used
     * instead of this method. If, on the other hand, scene changes involve
     * changes all within the same view hierarchy, among views which do not
     * necessarily have ids set on them, then the target list of views may be more
     * convenient.</p>
     *
     * @param target A View on which the Transition will act, must be non-null.
     * @return The Transition to which the target is added.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).addTarget(someView);</code>
     * @see #addTarget(int)
     */
    @Nonnull
    public Transition addTarget(@Nonnull View target) {
        mTargets.add(target);
        return this;
    }

    /**
     * Adds the id of a target view that this Transition is interested in
     * animating. By default, there are no targetIds, and a Transition will
     * listen for changes on every view in the hierarchy below the sceneRoot
     * of the Scene being transitioned into. Setting targetIds constrains
     * the Transition to only listen for, and act on, views with these IDs.
     * Views with different IDs, or no IDs whatsoever, will be ignored.
     *
     * <p>Note that using ids to specify targets implies that ids should be unique
     * within the view hierarchy underneath the scene root.</p>
     *
     * @param targetId The id of a target view, must be a positive number.
     * @return The Transition to which the targetId is added.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).addTarget(someId);</code>
     * @see View#getId()
     */
    @Nonnull
    public Transition addTarget(int targetId) {
        if (targetId > 0) {
            mTargetIds.add(targetId);
        }
        return this;
    }

    /**
     * Adds the transitionName of a target view that this Transition is interested in
     * animating. By default, there are no targetNames, and a Transition will
     * listen for changes on every view in the hierarchy below the sceneRoot
     * of the Scene being transitioned into. Setting targetNames constrains
     * the Transition to only listen for, and act on, views with these transitionNames.
     * Views with different transitionNames, or no transitionName whatsoever, will be ignored.
     *
     * <p>Note that transitionNames should be unique within the view hierarchy.</p>
     *
     * @param targetName The transitionName of a target view, must be non-null.
     * @return The Transition to which the target transitionName is added.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).addTarget(someName);</code>
     * @see View#getTransitionName()
     */
    @Nonnull
    public Transition addTarget(@Nonnull String targetName) {
        if (mTargetNames == null) {
            mTargetNames = new ArrayList<>();
        }
        mTargetNames.add(targetName);
        return this;
    }

    /**
     * Adds the Class of a target view that this Transition is interested in
     * animating. By default, there are no targetTypes, and a Transition will
     * listen for changes on every view in the hierarchy below the sceneRoot
     * of the Scene being transitioned into. Setting targetTypes constrains
     * the Transition to only listen for, and act on, views with these classes.
     * Views with different classes will be ignored.
     *
     * <p>Note that any View that can be cast to targetType will be included, so
     * if targetType is <code>View.class</code>, all Views will be included.</p>
     *
     * @param targetType The type to include when running this transition.
     * @return The Transition to which the target class was added.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).addTarget(ImageView.class);</code>
     * @see #addTarget(int)
     * @see #addTarget(View)
     * @see #excludeTarget(Class, boolean)
     * @see #excludeChildren(Class, boolean)
     */
    @Nonnull
    public Transition addTarget(@Nonnull Class<?> targetType) {
        if (mTargetTypes == null) {
            mTargetTypes = new ArrayList<>();
        }
        mTargetTypes.add(targetType);
        return this;
    }

    /**
     * Removes the given target from the list of targets that this Transition
     * is interested in animating.
     *
     * @param target The target view, must be non-null.
     * @return Transition The Transition from which the target is removed.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).removeTarget(someView);</code>
     */
    @Nonnull
    public Transition removeTarget(@Nonnull View target) {
        mTargets.remove(target);
        return this;
    }

    /**
     * Removes the given targetId from the list of ids that this Transition
     * is interested in animating.
     *
     * @param targetId The id of a target view, must be a positive number.
     * @return The Transition from which the targetId is removed.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).removeTargetId(someId);</code>
     */
    @Nonnull
    public Transition removeTarget(int targetId) {
        if (targetId > 0) {
            mTargetIds.rem(targetId);
        }
        return this;
    }

    /**
     * Removes the given targetName from the list of transitionNames that this Transition
     * is interested in animating.
     *
     * @param targetName The transitionName of a target view, must not be null.
     * @return The Transition from which the targetName is removed.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).removeTargetName(someName);</code>
     */
    @Nonnull
    public Transition removeTarget(@Nonnull String targetName) {
        if (mTargetNames != null) {
            mTargetNames.remove(targetName);
        }
        return this;
    }

    /**
     * Removes the given target from the list of targets that this Transition
     * is interested in animating.
     *
     * @param target The type of the target view, must be non-null.
     * @return Transition The Transition from which the target is removed.
     * Returning the same object makes it easier to chain calls during
     * construction, such as
     * <code>transitionSet.addTransitions(new Fade()).removeTarget(someType);</code>
     */
    @Nonnull
    public Transition removeTarget(@Nonnull Class<?> target) {
        if (mTargetTypes != null) {
            mTargetTypes.remove(target);
        }
        return this;
    }

    /**
     * Utility method to manage the boilerplate code that is the same whether we
     * are excluding targets or their children.
     */
    @Nullable
    private static <T> ArrayList<T> excludeObject(@Nullable ArrayList<T> list, T target,
                                                  boolean exclude) {
        if (target != null) {
            if (exclude) {
                list = ArrayListManager.add(list, target);
            } else {
                list = ArrayListManager.remove(list, target);
            }
        }
        return list;
    }

    /**
     * Whether to add the given target to the list of targets to exclude from this
     * transition. The <code>exclude</code> parameter specifies whether the target
     * should be added to or removed from the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param target  The target to ignore when running this transition.
     * @param exclude Whether to add the target to or remove the target from the
     *                current list of excluded targets.
     * @return This transition object.
     * @see #excludeChildren(View, boolean)
     * @see #excludeTarget(int, boolean)
     * @see #excludeTarget(Class, boolean)
     */
    @Nonnull
    public Transition excludeTarget(@Nonnull View target, boolean exclude) {
        mTargetExcludes = excludeView(mTargetExcludes, target, exclude);
        return this;
    }

    /**
     * Whether to add the given id to the list of target ids to exclude from this
     * transition. The <code>exclude</code> parameter specifies whether the target
     * should be added to or removed from the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param targetId The id of a target to ignore when running this transition.
     * @param exclude  Whether to add the target to or remove the target from the
     *                 current list of excluded targets.
     * @return This transition object.
     * @see #excludeChildren(int, boolean)
     * @see #excludeTarget(View, boolean)
     * @see #excludeTarget(Class, boolean)
     */
    @Nonnull
    public Transition excludeTarget(int targetId, boolean exclude) {
        mTargetIdExcludes = excludeId(mTargetIdExcludes, targetId, exclude);
        return this;
    }

    /**
     * Whether to add the given transitionName to the list of target transitionNames to exclude
     * from this transition. The <code>exclude</code> parameter specifies whether the target
     * should be added to or removed from the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded by their
     * id, their instance reference, their transitionName, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param targetName The name of a target to ignore when running this transition.
     * @param exclude    Whether to add the target to or remove the target from the
     *                   current list of excluded targets.
     * @return This transition object.
     * @see #excludeTarget(View, boolean)
     * @see #excludeTarget(int, boolean)
     * @see #excludeTarget(Class, boolean)
     */
    @Nonnull
    public Transition excludeTarget(@Nonnull String targetName, boolean exclude) {
        mTargetNameExcludes = excludeObject(mTargetNameExcludes, targetName, exclude);
        return this;
    }

    /**
     * Whether to add the children of given target to the list of target children
     * to exclude from this transition. The <code>exclude</code> parameter specifies
     * whether the target should be added to or removed from the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param target  The target to ignore when running this transition.
     * @param exclude Whether to add the target to or remove the target from the
     *                current list of excluded targets.
     * @return This transition object.
     * @see #excludeTarget(View, boolean)
     * @see #excludeChildren(int, boolean)
     * @see #excludeChildren(Class, boolean)
     */
    @Nonnull
    public Transition excludeChildren(@Nonnull View target, boolean exclude) {
        mTargetChildExcludes = excludeView(mTargetChildExcludes, target, exclude);
        return this;
    }

    /**
     * Whether to add the children of the given id to the list of targets to exclude
     * from this transition. The <code>exclude</code> parameter specifies whether
     * the children of the target should be added to or removed from the excluded list.
     * Excluding children in this way provides a simple mechanism for excluding all
     * children of specific targets, rather than individually excluding each
     * child individually.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param targetId The id of a target whose children should be ignored when running
     *                 this transition.
     * @param exclude  Whether to add the target to or remove the target from the
     *                 current list of excluded-child targets.
     * @return This transition object.
     * @see #excludeTarget(int, boolean)
     * @see #excludeChildren(View, boolean)
     * @see #excludeChildren(Class, boolean)
     */
    @Nonnull
    public Transition excludeChildren(int targetId, boolean exclude) {
        mTargetIdChildExcludes = excludeId(mTargetIdChildExcludes, targetId, exclude);
        return this;
    }

    /**
     * Utility method to manage the boilerplate code that is the same whether we
     * are excluding targets or their children.
     */
    @Nullable
    private IntArrayList excludeId(@Nullable IntArrayList list, int targetId,
                                   boolean exclude) {
        if (targetId > 0) {
            if (exclude) {
                list = ArrayListManager.add(list, targetId);
            } else {
                list = ArrayListManager.remove(list, targetId);
            }
        }
        return list;
    }

    /**
     * Utility method to manage the boilerplate code that is the same whether we
     * are excluding targets or their children.
     */
    @Nullable
    private ArrayList<View> excludeView(@Nullable ArrayList<View> list, View target,
                                        boolean exclude) {
        if (target != null) {
            if (exclude) {
                list = ArrayListManager.add(list, target);
            } else {
                list = ArrayListManager.remove(list, target);
            }
        }
        return list;
    }

    /**
     * Whether to add the given type to the list of types to exclude from this
     * transition. The <code>exclude</code> parameter specifies whether the target
     * type should be added to or removed from the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param type    The type to ignore when running this transition.
     * @param exclude Whether to add the target type to or remove it from the
     *                current list of excluded target types.
     * @return This transition object.
     * @see #excludeChildren(Class, boolean)
     * @see #excludeTarget(int, boolean)
     * @see #excludeTarget(View, boolean)
     */
    @Nonnull
    public Transition excludeTarget(@Nonnull Class<?> type, boolean exclude) {
        mTargetTypeExcludes = excludeType(mTargetTypeExcludes, type, exclude);
        return this;
    }

    /**
     * Whether to add the given type to the list of types whose children should
     * be excluded from this transition. The <code>exclude</code> parameter
     * specifies whether the target type should be added to or removed from
     * the excluded list.
     *
     * <p>Excluding targets is a general mechanism for allowing transitions to run on
     * a view hierarchy while skipping target views that should not be part of
     * the transition. For example, you may want to avoid animating children
     * of a specific ListView or Spinner. Views can be excluded either by their
     * id, or by their instance reference, or by the Class of that view
     * (eg, {@link icyllis.modernui.widget.Spinner}).</p>
     *
     * @param type    The type to ignore when running this transition.
     * @param exclude Whether to add the target type to or remove it from the
     *                current list of excluded target types.
     * @return This transition object.
     * @see #excludeTarget(Class, boolean)
     * @see #excludeChildren(int, boolean)
     * @see #excludeChildren(View, boolean)
     */
    @Nonnull
    public Transition excludeChildren(@Nonnull Class<?> type, boolean exclude) {
        mTargetTypeChildExcludes = excludeType(mTargetTypeChildExcludes, type, exclude);
        return this;
    }

    /**
     * Utility method to manage the boilerplate code that is the same whether we
     * are excluding targets or their children.
     */
    @Nullable
    private ArrayList<Class<?>> excludeType(@Nullable ArrayList<Class<?>> list, Class<?> type,
                                            boolean exclude) {
        if (type != null) {
            if (exclude) {
                list = ArrayListManager.add(list, type);
            } else {
                list = ArrayListManager.remove(list, type);
            }
        }
        return list;
    }

    /**
     * Returns the array of target IDs that this transition limits itself to
     * tracking and animating. If the array is null for both this method and
     * {@link #getTargets()}, then this transition is
     * not limited to specific views, and will handle changes to any views
     * in the hierarchy of a scene change.
     *
     * @return the list of target IDs
     */
    @Nonnull
    public IntList getTargetIds() {
        return mTargetIds;
    }

    /**
     * Returns the array of target views that this transition limits itself to
     * tracking and animating. If the array is null for both this method and
     * {@link #getTargetIds()}, then this transition is
     * not limited to specific views, and will handle changes to any views
     * in the hierarchy of a scene change.
     *
     * @return the list of target views
     */
    @Nonnull
    public List<View> getTargets() {
        return mTargets;
    }

    /**
     * Returns the list of target transitionNames that this transition limits itself to
     * tracking and animating. If the list is null or empty for
     * {@link #getTargetIds()}, {@link #getTargets()}, {@link #getTargetTypes()}, and
     * this method then this transition is
     * not limited to specific views, and will handle changes to any views
     * in the hierarchy of a scene change.
     *
     * @return the list of target transitionNames
     */
    @Nullable
    public List<String> getTargetNames() {
        return mTargetNames;
    }

    /**
     * Returns the list of target transitionNames that this transition limits itself to
     * tracking and animating. If the list is null or empty for
     * {@link #getTargetIds()}, {@link #getTargets()}, {@link #getTargetNames()}, and
     * this method then this transition is
     * not limited to specific views, and will handle changes to any views
     * in the hierarchy of a scene change.
     *
     * @return the list of target Types
     */
    @Nullable
    public List<Class<?>> getTargetTypes() {
        return mTargetTypes;
    }

    /**
     * Recursive method that captures values for the given view and the
     * hierarchy underneath it.
     *
     * @param sceneRoot The root of the view hierarchy being captured
     * @param start     true if this capture is happening before the scene change,
     *                  false otherwise
     */
    void captureValues(ViewGroup sceneRoot, boolean start) {
        clearValues(start);
        if ((mTargetIds.size() > 0 || mTargets.size() > 0)
                && (mTargetNames == null || mTargetNames.isEmpty())
                && (mTargetTypes == null || mTargetTypes.isEmpty())) {
            for (int id : mTargetIds) {
                View view = sceneRoot.findViewById(id);
                if (view != null) {
                    TransitionValues values = new TransitionValues(view);
                    if (start) {
                        captureStartValues(values);
                    } else {
                        captureEndValues(values);
                    }
                    values.mTargetedTransitions.add(this);
                    capturePropagationValues(values);
                    if (start) {
                        addViewValues(mStartValues, view, values);
                    } else {
                        addViewValues(mEndValues, view, values);
                    }
                }
            }
            for (View view : mTargets) {
                TransitionValues values = new TransitionValues(view);
                if (start) {
                    captureStartValues(values);
                } else {
                    captureEndValues(values);
                }
                values.mTargetedTransitions.add(this);
                capturePropagationValues(values);
                if (start) {
                    addViewValues(mStartValues, view, values);
                } else {
                    addViewValues(mEndValues, view, values);
                }
            }
        } else {
            captureHierarchy(sceneRoot, start);
        }
    }

    private static void addViewValues(@Nonnull TransitionValuesMaps transitionValuesMaps,
                                      @Nonnull View view, TransitionValues transitionValues) {
        transitionValuesMaps.mViewValues.put(view, transitionValues);
        int id = view.getId();
        if (id >= 0) {
            if (transitionValuesMaps.mIdValues.indexOfKey(id) >= 0) {
                // Duplicate IDs cannot match by ID.
                transitionValuesMaps.mIdValues.put(id, null);
            } else {
                transitionValuesMaps.mIdValues.put(id, view);
            }
        }
        String name = view.getTransitionName();
        if (name != null) {
            if (transitionValuesMaps.mNameValues.containsKey(name)) {
                // Duplicate transitionNames: cannot match by transitionName.
                transitionValuesMaps.mNameValues.put(name, null);
            } else {
                transitionValuesMaps.mNameValues.put(name, view);
            }
        }
        if (view.getParent() instanceof ListView listview) {
            if (listview.getAdapter().hasStableIds()) {
                int position = listview.getPositionForView(view);
                long itemId = listview.getItemIdAtPosition(position);
                if (transitionValuesMaps.mItemIdValues.indexOfKey(itemId) >= 0) {
                    // Duplicate item IDs: cannot match by item ID.
                    View alreadyMatched = transitionValuesMaps.mItemIdValues.get(itemId);
                    if (alreadyMatched != null) {
                        alreadyMatched.setHasTransientState(false);
                        transitionValuesMaps.mItemIdValues.put(itemId, null);
                    }
                } else {
                    view.setHasTransientState(true);
                    transitionValuesMaps.mItemIdValues.put(itemId, view);
                }
            }
        }
    }

    /**
     * Clear valuesMaps for specified start/end state
     *
     * @param start true if the start values should be cleared, false otherwise
     */
    void clearValues(boolean start) {
        if (start) {
            mStartValues.mViewValues.clear();
            mStartValues.mIdValues.clear();
            mStartValues.mItemIdValues.clear();
            mStartValues.mNameValues.clear();
            mStartValuesList = null;
        } else {
            mEndValues.mViewValues.clear();
            mEndValues.mIdValues.clear();
            mEndValues.mItemIdValues.clear();
            mEndValues.mNameValues.clear();
            mEndValuesList = null;
        }
    }

    /**
     * Recursive method which captures values for an entire view hierarchy,
     * starting at some root view. Transitions without targetIDs will use this
     * method to capture values for all possible views.
     *
     * @param view  The view for which to capture values. Children of this View
     *              will also be captured, recursively down to the leaf nodes.
     * @param start true if values are being captured in the start scene, false
     *              otherwise.
     */
    private void captureHierarchy(View view, boolean start) {
        if (view == null) {
            return;
        }
        int id = view.getId();
        if (mTargetIdExcludes != null && mTargetIdExcludes.contains(id)) {
            return;
        }
        if (mTargetExcludes != null && mTargetExcludes.contains(view)) {
            return;
        }
        if (mTargetTypeExcludes != null) {
            for (Class<?> type : mTargetTypeExcludes) {
                if (type.isInstance(view)) {
                    return;
                }
            }
        }
        if (view.getParent() instanceof ViewGroup) {
            TransitionValues values = new TransitionValues(view);
            if (start) {
                captureStartValues(values);
            } else {
                captureEndValues(values);
            }
            values.mTargetedTransitions.add(this);
            capturePropagationValues(values);
            if (start) {
                addViewValues(mStartValues, view, values);
            } else {
                addViewValues(mEndValues, view, values);
            }
        }
        if (view instanceof ViewGroup parent) {
            // Don't traverse child hierarchy if there are any child-excludes on this view
            if (mTargetIdChildExcludes != null && mTargetIdChildExcludes.contains(id)) {
                return;
            }
            if (mTargetChildExcludes != null && mTargetChildExcludes.contains(view)) {
                return;
            }
            if (mTargetTypeChildExcludes != null) {
                for (Class<?> targetTypeChildExclude : mTargetTypeChildExcludes) {
                    if (targetTypeChildExclude.isInstance(view)) {
                        return;
                    }
                }
            }
            for (int i = 0; i < parent.getChildCount(); ++i) {
                captureHierarchy(parent.getChildAt(i), start);
            }
        }
    }

    /**
     * This method can be called by transitions to get the TransitionValues for
     * any particular view during the transition-playing process. This might be
     * necessary, for example, to query the before/after state of related views
     * for a given transition.
     */
    @Nullable
    public TransitionValues getTransitionValues(@Nonnull View view, boolean start) {
        if (mParent != null) {
            return mParent.getTransitionValues(view, start);
        }
        TransitionValuesMaps valuesMaps = start ? mStartValues : mEndValues;
        return valuesMaps.mViewValues.get(view);
    }

    /**
     * Find the matched start or end value for a given View. This is only valid
     * after playTransition starts. For example, it will be valid in
     * {@link #createAnimator(ViewGroup, TransitionValues, TransitionValues)}, but not
     * in {@link #captureStartValues(TransitionValues)}.
     *
     * @param view        The view to find the match for.
     * @param viewInStart Is View from the start values or end values.
     * @return The matching TransitionValues for view in either start or end values, depending
     * on viewInStart or null if there is no match for the given view.
     */
    @Nullable
    TransitionValues getMatchedTransitionValues(@Nonnull View view, boolean viewInStart) {
        if (mParent != null) {
            return mParent.getMatchedTransitionValues(view, viewInStart);
        }
        ArrayList<TransitionValues> lookIn = viewInStart ? mStartValuesList : mEndValuesList;
        if (lookIn == null) {
            return null;
        }
        int count = lookIn.size();
        int index = -1;
        for (int i = 0; i < count; i++) {
            TransitionValues values = lookIn.get(i);
            if (values == null) {
                // Null values are always added to the end of the list, so we know to stop now.
                return null;
            }
            if (values.view == view) {
                index = i;
                break;
            }
        }
        TransitionValues values = null;
        if (index >= 0) {
            ArrayList<TransitionValues> matchIn = viewInStart ? mEndValuesList : mStartValuesList;
            values = matchIn.get(index);
        }
        return values;
    }

    /**
     * Pauses this transition, sending out calls to {@link
     * TransitionListener#onTransitionPause(Transition)} to all listeners
     * and pausing all running animators started by this transition.
     *
     * @hide
     */
    public void pause(View sceneRoot) {
        if (!mEnded) {
            int numAnimators = mCurrentAnimators.size();
            for (int i = numAnimators - 1; i >= 0; i--) {
                Animator animator = mCurrentAnimators.get(i);
                animator.pause();
            }
            if (mListeners != null && mListeners.size() > 0) {
                for (TransitionListener listener : mListeners) {
                    listener.onTransitionPause(this);
                }
            }
            mPaused = true;
        }
    }

    /**
     * Resumes this transition, sending out calls to {@link
     * TransitionListener#onTransitionPause(Transition)} to all listeners
     * and pausing all running animators started by this transition.
     */
    protected void resume(@Nonnull View sceneRoot) {
        if (mPaused) {
            if (!mEnded) {
                for (int i = mCurrentAnimators.size() - 1; i >= 0; i--) {
                    mCurrentAnimators.get(i).resume();
                }
                if (mListeners != null && mListeners.size() > 0) {
                    for (TransitionListener listener : mListeners) {
                        listener.onTransitionResume(this);
                    }
                }
            }
            mPaused = false;
        }
    }

    /**
     * Called by TransitionManager to play the transition. This calls
     * createAnimators() to set things up and create all the animations and then
     * runAnimations() to actually start the animations.
     */
    void playTransition(@Nonnull ViewGroup sceneRoot) {
        mStartValuesList = new ArrayList<>();
        mEndValuesList = new ArrayList<>();
        matchStartAndEnd(mStartValues, mEndValues);

        final ArrayMap<Animator, AnimationInfo> runningAnimators = sRunningAnimators.get();
        for (int i = runningAnimators.size() - 1; i >= 0; i--) {
            final Animator anim = runningAnimators.keyAt(i);
            if (anim == null) {
                continue;
            }
            final AnimationInfo oldInfo = runningAnimators.get(anim);
            if (oldInfo != null && oldInfo.view != null) {
                TransitionValues oldValues = oldInfo.values;
                View oldView = oldInfo.view;
                TransitionValues startValues = getTransitionValues(oldView, true);
                TransitionValues endValues = getMatchedTransitionValues(oldView, true);
                if (startValues == null && endValues == null) {
                    endValues = mEndValues.mViewValues.get(oldView);
                }
                boolean cancel = (startValues != null || endValues != null) &&
                        oldInfo.transition.isTransitionRequired(oldValues, endValues);
                if (cancel) {
                    if (anim.isRunning() || anim.isStarted()) {
                        anim.cancel();
                    } else {
                        runningAnimators.remove(anim);
                    }
                }
            }
        }

        createAnimators(sceneRoot, mStartValues, mEndValues, mStartValuesList, mEndValuesList);
        runAnimators();
    }

    /**
     * Returns whether the transition should create an Animator, based on the values
     * captured during {@link #captureStartValues(TransitionValues)} and
     * {@link #captureEndValues(TransitionValues)}. The default implementation compares the
     * property values returned from {@link #getTransitionProperties()}, or all property values if
     * {@code getTransitionProperties()} returns null. Subclasses may override this method to
     * provide logic more specific to the transition implementation.
     *
     * @param startValues the values from captureStartValues, This may be {@code null} if the
     *                    View did not exist in the start state.
     * @param endValues   the values from captureEndValues. This may be {@code null} if the View
     *                    did not exist in the end state.
     */
    public boolean isTransitionRequired(@Nullable TransitionValues startValues,
                                        @Nullable TransitionValues endValues) {
        boolean valuesChanged = false;
        // if startValues null, then transition didn't care to stash values,
        // and won't get canceled
        if (startValues != null && endValues != null) {
            String[] properties = getTransitionProperties();
            if (properties != null) {
                for (String property : properties) {
                    if (isValueChanged(startValues, endValues, property)) {
                        valuesChanged = true;
                        break;
                    }
                }
            } else {
                for (String key : startValues.values.keySet()) {
                    if (isValueChanged(startValues, endValues, key)) {
                        valuesChanged = true;
                        break;
                    }
                }
            }
        }
        return valuesChanged;
    }

    private static boolean isValueChanged(@Nonnull TransitionValues oldValues,
                                          @Nonnull TransitionValues newValues,
                                          @Nonnull String key) {
        if (oldValues.values.containsKey(key) != newValues.values.containsKey(key)) {
            // The transition didn't care about this particular value, so we don't care, either.
            return false;
        }
        Object oldValue = oldValues.values.get(key);
        Object newValue = newValues.values.get(key);
        boolean changed;
        if (oldValue == null && newValue == null) {
            // both are null
            changed = false;
        } else if (oldValue == null || newValue == null) {
            // one is null
            changed = true;
        } else {
            // neither is null
            changed = !oldValue.equals(newValue);
        }
        return changed;
    }

    /**
     * This is a utility method used by subclasses to handle standard parts of
     * setting up and running an Animator: it sets the {@link #getDuration()
     * duration} and the {@link #getStartDelay() startDelay}, starts the
     * animation, and, when the animator ends, calls {@link #end()}.
     *
     * @param animator The Animator to be run during this transition.
     */
    protected void animate(@Nullable Animator animator) {
        // TODO: maybe pass auto-end as a boolean parameter?
        if (animator == null) {
            end();
        } else {
            if (getDuration() >= 0) {
                animator.setDuration(getDuration());
            }
            if (getStartDelay() >= 0) {
                animator.setStartDelay(getStartDelay() + animator.getStartDelay());
            }
            if (getInterpolator() != null) {
                animator.setInterpolator(getInterpolator());
            }
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationEnd(@Nonnull Animator animation) {
                    end();
                    animation.removeListener(this);
                }
            });
            animator.start();
        }
    }

    /**
     * This method is called automatically by the transition and
     * TransitionSet classes prior to a Transition subclass starting;
     * subclasses should not need to call it directly.
     */
    protected void start() {
        if (mNumInstances == 0) {
            if (mListeners != null && mListeners.size() > 0) {
                for (TransitionListener listener : mListeners) {
                    listener.onTransitionStart(this);
                }
            }
            mEnded = false;
        }
        mNumInstances++;
    }

    /**
     * This method is called automatically by the Transition and TransitionSet classes
     * when a transition finishes, either because a transition did nothing (returned
     * a null Animator from
     * {@link Transition#createAnimator(ViewGroup, TransitionValues, TransitionValues)})
     * or because the transition returned a valid Animator and end() was called in the
     * onAnimationEnd() callback of the AnimatorListener.
     */
    protected void end() {
        --mNumInstances;
        if (mNumInstances == 0) {
            if (mListeners != null && mListeners.size() > 0) {
                for (TransitionListener listener : mListeners) {
                    listener.onTransitionEnd(this);
                }
            }
            for (int i = 0; i < mStartValues.mItemIdValues.size(); ++i) {
                View view = mStartValues.mItemIdValues.valueAt(i);
                if (view != null) {
                    view.setHasTransientState(false);
                }
            }
            for (int i = 0; i < mEndValues.mItemIdValues.size(); ++i) {
                View view = mEndValues.mItemIdValues.valueAt(i);
                if (view != null) {
                    view.setHasTransientState(false);
                }
            }
            mEnded = true;
        }
    }

    /**
     * Force the transition to move to its end state, ending all the animators.
     */
    void forceToEnd(@Nonnull ViewGroup sceneRoot) {
        final ArrayMap<Animator, AnimationInfo> runningAnimators = sRunningAnimators.get();
        int size = runningAnimators.size();
        if (size == 0) {
            return;
        }

        final ArrayMap<Animator, AnimationInfo> animators = new ArrayMap<>(runningAnimators);
        runningAnimators.clear();

        for (int i = size - 1; i >= 0; i--) {
            AnimationInfo info = animators.valueAt(i);
            if (info.view != null) {
                animators.keyAt(i).end();
            }
        }
    }

    /**
     * This method cancels a transition that is currently running.
     */
    public void cancel() {
        for (int i = mCurrentAnimators.size() - 1; i >= 0; i--) {
            mCurrentAnimators.get(i).cancel();
        }
        if (mListeners != null && mListeners.size() > 0) {
            for (TransitionListener listener : mListeners) {
                listener.onTransitionCancel(this);
            }
        }
    }

    /**
     * Adds a listener to the set of listeners that are sent events through the
     * life of an animation, such as start, repeat, and end.
     *
     * @param listener the listener to be added to the current set of listeners
     *                 for this animation.
     * @return This transition object.
     */
    @Nonnull
    public Transition addListener(@Nonnull TransitionListener listener) {
        if (mListeners == null) {
            mListeners = new CopyOnWriteArrayList<>();
        }
        mListeners.addIfAbsent(listener);
        return this;
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of
     *                 listeners for this transition.
     * @return This transition object.
     */
    @Nonnull
    public Transition removeListener(@Nonnull TransitionListener listener) {
        if (mListeners == null) {
            return this;
        }
        mListeners.remove(listener);
        if (mListeners.isEmpty()) {
            mListeners = null;
        }
        return this;
    }

    /**
     * Sets the callback to use to find the epicenter of a Transition. A null value indicates
     * that there is no epicenter in the Transition and onGetEpicenter() will return null.
     * Transitions like {@link Explode} use a point or Rect to orient
     * the direction of travel. This is called the epicenter of the Transition and is
     * typically centered on a touched View. The
     * {@link EpicenterCallback} allows a Transition to
     * dynamically retrieve the epicenter during a Transition.
     *
     * @param epicenterCallback The callback to use to find the epicenter of the Transition.
     */
    public void setEpicenterCallback(@Nullable EpicenterCallback epicenterCallback) {
        mEpicenterCallback = epicenterCallback;
    }

    /**
     * Returns the callback used to find the epicenter of the Transition.
     * Transitions like {@link Explode} use a point or Rect to orient
     * the direction of travel. This is called the epicenter of the Transition and is
     * typically centered on a touched View. The
     * {@link EpicenterCallback} allows a Transition to
     * dynamically retrieve the epicenter during a Transition.
     *
     * @return the callback used to find the epicenter of the Transition.
     */
    @Nullable
    public EpicenterCallback getEpicenterCallback() {
        return mEpicenterCallback;
    }

    /**
     * Returns the epicenter as specified by the
     * {@link EpicenterCallback} or null if no callback exists.
     *
     * @return the epicenter as specified by the
     * {@link EpicenterCallback} or null if no callback exists.
     * @see #setEpicenterCallback(EpicenterCallback)
     */
    @Nullable
    public Rect getEpicenter() {
        if (mEpicenterCallback == null) {
            return null;
        }
        return mEpicenterCallback.onGetEpicenter(this);
    }

    /**
     * Sets the method for determining Animator start delays.
     * <p>
     * When a Transition affects several Views like {@link Explode} or
     * {@link Slide}, there may be a desire to have a "wave-front" effect
     * such that the Animator start delay depends on position of the View. The
     * TransitionPropagation specifies how the start delays are calculated.
     *
     * @param transitionPropagation The class used to determine the start delay of
     *                              Animators created by this Transition. A null value
     *                              indicates that no delay should be used.
     */
    public void setPropagation(@Nullable TransitionPropagation transitionPropagation) {
        mPropagation = transitionPropagation;
    }

    /**
     * Returns the {@link TransitionPropagation} used to calculate Animator
     * start delays.
     * <p>
     * When a Transition affects several Views like {@link Explode} or
     * {@link Slide}, there may be a desire to have a "wave-front" effect
     * such that the Animator start delay depends on position of the View. The
     * TransitionPropagation specifies how the start delays are calculated.
     *
     * @return the {@link TransitionPropagation} used to calculate Animator start
     * delays. This is null by default.
     */
    @Nullable
    public TransitionPropagation getPropagation() {
        return mPropagation;
    }

    /**
     * Captures TransitionPropagation values for the given view and the
     * hierarchy underneath it.
     */
    void capturePropagationValues(@Nonnull TransitionValues transitionValues) {
        if (mPropagation != null && !transitionValues.values.isEmpty()) {
            String[] propertyNames = mPropagation.getPropagationProperties();
            if (propertyNames == null) {
                return;
            }
            boolean containsAll = true;
            for (String propertyName : propertyNames) {
                if (!transitionValues.values.containsKey(propertyName)) {
                    containsAll = false;
                    break;
                }
            }
            if (!containsAll) {
                mPropagation.captureValues(transitionValues);
            }
        }
    }

    void setCanRemoveViews(boolean canRemoveViews) {
        mCanRemoveViews = canRemoveViews;
    }

    /**
     * Returns the name of this Transition. This name is used internally to distinguish
     * between different transitions to determine when interrupting transitions overlap.
     * For example, a ChangeBounds running on the same target view as another ChangeBounds
     * should determine whether the old transition is animating to different end values
     * and should be canceled in favor of the new transition.
     *
     * <p>By default, a Transition's name is simply the value of {@link Class#getName()},
     * but subclasses are free to override and return something different.</p>
     *
     * @return The name of this transition.
     */
    @Nonnull
    public String getName() {
        return getClass().getName();
    }

    @Override
    public String toString() {
        return toString("");
    }

    String toString(String indent) {
        StringBuilder sb = new StringBuilder(indent + getClass().getSimpleName() + "@"
                + Integer.toHexString(hashCode()) + ": ");
        if (mDuration != -1) {
            sb.append("duration(").append(mDuration).append(") ");
        }
        if (mStartDelay != -1) {
            sb.append("delay(").append(mStartDelay).append(") ");
        }
        if (mInterpolator != null) {
            sb.append("interpolator(").append(mInterpolator).append(") ");
        }
        if (mTargetIds.size() > 0 || mTargets.size() > 0) {
            sb.append("targets(");
            if (mTargetIds.size() > 0) {
                for (int i = 0; i < mTargetIds.size(); ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(mTargetIds.getInt(i));
                }
            }
            if (mTargets.size() > 0) {
                for (int i = 0; i < mTargets.size(); ++i) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(mTargets.get(i));
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public Transition clone() {
        try {
            Transition clone = (Transition) super.clone();
            clone.mAnimators = new ArrayList<>();
            clone.mStartValues = new TransitionValuesMaps();
            clone.mEndValues = new TransitionValuesMaps();
            clone.mStartValuesList = null;
            clone.mEndValuesList = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * Holds information about each animator used when a new transition starts
     * while other transitions are still running to determine whether a running
     * animation should be canceled or a new animation noop'd. The structure holds
     * information about the state that an animation is going to, to be compared to
     * end state of a new animation.
     */
    private record AnimationInfo(View view, String name, Transition transition, TransitionValues values) {
    }

    static final class TransitionValuesMaps {

        final ArrayMap<View, TransitionValues> mViewValues = new ArrayMap<>();
        final SparseArray<View> mIdValues = new SparseArray<>();
        final LongSparseArray<View> mItemIdValues = new LongSparseArray<>();
        final ArrayMap<String, View> mNameValues = new ArrayMap<>();
    }

    /**
     * Utility class for managing typed ArrayLists efficiently. In particular, this
     * can be useful for lists that we don't expect to be used often (eg, the exclude
     * lists), so we'd like to keep them nulled out by default. This causes the code to
     * become tedious, with constant null checks, code to allocate when necessary,
     * and code to null out the reference when the list is empty. This class encapsulates
     * all of that functionality into simple add()/remove() methods which perform the
     * necessary checks, allocation/null-out as appropriate, and return the
     * resulting list.
     */
    private static class ArrayListManager {

        /**
         * Add the specified item to the list, returning the resulting list.
         * The returned list can either be the same list passed in or, if that
         * list was null, the new list that was created.
         * <p>
         * Note that the list holds unique items; if the item already exists in the
         * list, the list is not modified.
         */
        @Nonnull
        static <T> ArrayList<T> add(@Nullable ArrayList<T> list, T item) {
            if (list == null) {
                list = new ArrayList<>();
            }
            if (!list.contains(item)) {
                list.add(item);
            }
            return list;
        }

        /**
         * Add the specified item to the list, returning the resulting list.
         * The returned list can either be the same list passed in or, if that
         * list was null, the new list that was created.
         * <p>
         * Note that the list holds unique items; if the item already exists in the
         * list, the list is not modified.
         */
        @Nonnull
        static IntArrayList add(@Nullable IntArrayList list, int item) {
            if (list == null) {
                list = new IntArrayList();
            }
            if (!list.contains(item)) {
                list.add(item);
            }
            return list;
        }

        /**
         * Remove the specified item from the list, returning the resulting list.
         * The returned list can either be the same list passed in or, if that
         * list becomes empty as a result of the remove(), the new list was created.
         */
        @Nullable
        static <T> ArrayList<T> remove(@Nullable ArrayList<T> list, T item) {
            if (list != null) {
                list.remove(item);
                if (list.isEmpty()) {
                    list = null;
                }
            }
            return list;
        }

        /**
         * Remove the specified item from the list, returning the resulting list.
         * The returned list can either be the same list passed in or, if that
         * list becomes empty as a result of the remove(), the new list was created.
         */
        @Nullable
        static IntArrayList remove(@Nullable IntArrayList list, int item) {
            if (list != null) {
                list.rem(item);
                if (list.isEmpty()) {
                    list = null;
                }
            }
            return list;
        }
    }

    /**
     * Interface to get the epicenter of Transition. Use
     * {@link #setEpicenterCallback(EpicenterCallback)} to set the callback used to calculate the
     * epicenter of the Transition. Override {@link #getEpicenter()} to return the rectangular
     * region in screen coordinates of the epicenter of the transition.
     *
     * @see #setEpicenterCallback(EpicenterCallback)
     */
    @FunctionalInterface
    public interface EpicenterCallback {

        /**
         * Implementers must override to return the epicenter of the Transition in screen
         * coordinates. Transitions like {@link Explode} depend upon
         * an epicenter for the Transition. In Explode, Views move toward or away from the
         * center of the epicenter Rect along the vector between the epicenter and the center
         * of the View appearing and disappearing. Some Transitions, such as
         * {@link Fade} pay no attention to the epicenter.
         *
         * @param transition The transition for which the epicenter applies.
         * @return The Rect region of the epicenter of <code>transition</code> or null if
         * there is no epicenter.
         */
        @Nullable
        Rect onGetEpicenter(@Nonnull Transition transition);
    }
}

package com.gwani.keyboard

import android.content.Context

// -----------------------------------------------------------
// GESTURE ENGINE
// One job: given a touch start and end point,
// return which direction the finger moved.
//
// Used by KeyboardView to decide:
//   - Was this a tap? (short distance = NONE)
//   - Was this a swipe? (long distance = UP/DOWN/LEFT/RIGHT)
// -----------------------------------------------------------

// All possible results from a gesture
enum class GestureDirection {
    NONE,   // finger didn't move far enough — treat as tap
    UP,     // finger moved upward
    DOWN,   // finger moved downward
    LEFT,   // finger moved left (spacebar → Flow Layer)
    RIGHT   // finger moved right (spacebar → Base Layer)
}

class GestureEngine(context: Context) {

    // Minimum distance in pixels before we count it as a swipe
    // 20dp converts to pixels using screen density
    // Below this = tap. Above this = swipe.
    private val minSwipeDistance = 20f * context.resources.displayMetrics.density

    // Where the finger first touched down
    private var startX = 0f
    private var startY = 0f

    // Call this when finger touches the screen (ACTION_DOWN)
    fun onTouchDown(x: Float, y: Float) {
        startX = x
        startY = y
    }

    // Call this when finger lifts (ACTION_UP)
    // Returns the direction — or NONE if it was just a tap
    fun onTouchUp(x: Float, y: Float): GestureDirection {
        val dx = x - startX  // horizontal distance (positive = moved right)
        val dy = y - startY  // vertical distance (positive = moved down)

        // Total distance moved using Pythagoras
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        // Too short — this was a tap not a swipe
        if (distance < minSwipeDistance) return GestureDirection.NONE

        // Determine direction:
        // Compare absolute horizontal vs absolute vertical movement
        // Whichever is larger tells us the main direction
        return if (Math.abs(dy) >= Math.abs(dx)) {
            // Vertical movement dominates
            if (dy < 0) GestureDirection.UP else GestureDirection.DOWN
        } else {
            // Horizontal movement dominates
            if (dx < 0) GestureDirection.LEFT else GestureDirection.RIGHT
        }
    }

    // Resets start position — call before reusing for a new touch
    fun reset() {
        startX = 0f
        startY = 0f
    }
}

package com.gwani.keyboard

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

// -----------------------------------------------------------
// BACKSPACE HANDLER
// Manages all backspace touch behavior in one dedicated place.
// Handles two scenarios:
//   1. Quick tap  — deletes one character immediately
//   2. Long press — deletes continuously every 50ms after 400ms hold
//
// How the loop stops cleanly:
// deleteRunnable checks isLongPressing before each delete.
// When finger lifts, isLongPressing = false.
// The next scheduled run sees false and stops itself.
// No need to chase down a running loop — it stops itself.
// -----------------------------------------------------------

class BackspaceHandler(
    // onDelete is a function we pass in from KeyboardView
    // When backspace needs to delete, it calls this
    // This keeps BackspaceHandler independent — it doesn't
    // know or care about InputConnection or GwaniIME
    private val onDelete: () -> Unit
) : View.OnTouchListener {

    private val handler = Handler(Looper.getMainLooper())
    private var isLongPressing = false

    // Runs repeatedly every 50ms while long pressing
    // Checks isLongPressing before each delete
    // If finger has lifted (isLongPressing = false) — stops itself
    private val deleteRunnable = object : Runnable {
        override fun run() {
            if (isLongPressing) {
                onDelete()
                handler.postDelayed(this, 50)
            }
        }
    }

    // Scheduled to run after 400ms of holding
    // If finger lifts before 400ms this gets cancelled
    // and long press never activates
    private val longPressRunnable = Runnable {
        isLongPressing = true
        handler.post(deleteRunnable)
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                isLongPressing = false

                // Delete one character immediately on first touch
                // This is the normal single tap delete
                onDelete()

                // Start the 400ms countdown to long press
                // If finger lifts before 400ms this is cancelled below
                handler.postDelayed(longPressRunnable, 400)
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Set flag to false FIRST
                // deleteRunnable checks this flag — setting it false
                // stops the loop on its very next check
                isLongPressing = false

                // Also cancel any pending callbacks that haven't fired yet
                handler.removeCallbacks(longPressRunnable)
                handler.removeCallbacks(deleteRunnable)

                view.performClick()
                return true
            }
        }
        return false
    }
}

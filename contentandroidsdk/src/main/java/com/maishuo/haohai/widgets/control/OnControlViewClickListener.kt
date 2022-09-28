package com.maishuo.haohai.widgets.control

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

interface OnControlViewClickListener {
    fun onPlayerModelClick(view: ImageView?) {

    }

    fun onPlayerListClick(view: ImageView?) {

    }

    fun onPlayerSpeedClick(view: TextView?) {

    }

    fun onPlayerPrevClick(view: ImageButton?) {

    }

    fun onPlayerNextClick(view: ImageButton?) {

    }

    fun onPlayerControlViewClick(view: ImageButton?) {

    }
}
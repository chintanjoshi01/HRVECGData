package com.example.polarecgdata.utils

import android.app.Activity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.example.polarecgdata.R


class ActionCallback(var activity: Activity, actionCallbackclick: ActionCallbackclick) :
    ActionMode.Callback {
    var actionCallbackclick: ActionCallbackclick

    init {
        this.actionCallbackclick = actionCallbackclick
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        toggleStatusBarColor(activity)
        mode.menuInflater.inflate(R.menu.menu_long, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delteItem -> {
                actionCallbackclick.onActionItemClickedCallback()
                mode.finish()
                return true
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionCallbackclick.onDestroyActionModeCallback()
    }
}

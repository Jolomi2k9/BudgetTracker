package com.example.budgettracker.util

import androidx.appcompat.widget.SearchView

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    //define what will happen when the query text changes
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        //
        override fun onQueryTextSubmit(query: String?): Boolean {
            //do nothing
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            //trigger when something is typed in the search view
            //also return an empty string if the newText function is null
            listener(newText.orEmpty())
            return true
        }
    })
}
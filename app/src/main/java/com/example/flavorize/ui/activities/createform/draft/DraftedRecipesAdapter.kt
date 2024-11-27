package com.example.flavorize.ui.activities.createform.draft

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flavorize.R
import com.example.flavorize.data.recipedraft.DraftRecipe

class DraftedRecipesAdapter(
    private val drafts: List<DraftRecipe>,
    private val onClick: (DraftRecipe) -> Unit
) : RecyclerView.Adapter<DraftedRecipesAdapter.DraftViewHolder>() {

    class DraftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.draftNameTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.draftDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_draft_recipe, parent, false)
        return DraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        val draft = drafts[position]
        holder.nameTextView.text = draft.name
        holder.descriptionTextView.text = draft.description

        holder.itemView.setOnClickListener {
            onClick(draft) // Kirim data draft ke activity
        }
    }

    override fun getItemCount(): Int = drafts.size
}

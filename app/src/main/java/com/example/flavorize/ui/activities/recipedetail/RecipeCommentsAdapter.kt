package com.example.flavorize.ui.activities.recipedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flavorize.R
import com.example.flavorize.data.RecipeComment
import com.example.flavorize.databinding.ItemCommentBinding

class RecipeCommentsAdapter(
    private val currentUserId: String,
    private val onCommentLongClick: (comment: RecipeComment, view: View) -> Unit
) : RecyclerView.Adapter<RecipeCommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<RecipeComment>()

    fun submitList(newComments: List<RecipeComment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)

        // Highlight comment if it belongs to the current user
        if (comment.userId == currentUserId) {
            holder.binding.commentContainer.setBackgroundResource(R.drawable.current_user_comment_background)

            // Allow long-click for user's own comments only
            holder.itemView.setOnLongClickListener {
                onCommentLongClick(comment, holder.itemView)
                true
            }
        } else {
            holder.binding.commentContainer.setBackgroundResource(R.drawable.comment_background)

            // Disable long-click for other users' comments
            holder.itemView.setOnLongClickListener(null)
        }
    }

    override fun getItemCount(): Int = comments.size

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: RecipeComment) {
            binding.userNameTextView.text = comment.userName
            binding.commentTextView.text = comment.text
            binding.timestampTextView.text = android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", comment.timestamp)
        }
    }
}

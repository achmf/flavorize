package com.example.flavorize.ui.activities.recipedetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flavorize.R
import com.example.flavorize.data.RecipeComment
import com.example.flavorize.databinding.ItemCommentBinding

class RecipeCommentsAdapter(
    private val currentUserId: String, // ID of the current user
    private val onCommentLongClick: (comment: RecipeComment, view: View) -> Unit // Callback for long click events
) : RecyclerView.Adapter<RecipeCommentsAdapter.CommentViewHolder>() {

    private val comments = mutableListOf<RecipeComment>() // List to store comments

    // Update the list of comments and refresh the UI
    fun submitList(newComments: List<RecipeComment>) {
        comments.clear()
        comments.addAll(newComments)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the comment item layout
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment) // Bind the comment data to the ViewHolder

        // Highlight comment if it belongs to the current user
        if (comment.userId == currentUserId) {
            holder.binding.commentContainer.setBackgroundResource(R.drawable.current_user_comment_background)

            // Enable long-click for user's own comments
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

    override fun getItemCount(): Int = comments.size // Return the total number of comments

    // ViewHolder to bind and display individual comment details
    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: RecipeComment) {
            // Set user name, comment text, and timestamp
            binding.userNameTextView.text = comment.userName
            binding.commentTextView.text = comment.text
            binding.timestampTextView.text = android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", comment.timestamp)
        }
    }
}

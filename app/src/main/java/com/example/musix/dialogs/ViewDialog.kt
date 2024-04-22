package com.example.musix.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.musix.R
import com.example.musix.activities.Login
import com.example.musix.handlers.GoogleSignInHelper
import com.example.musix.models.Artist
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth

class ViewDialog(private val context: Context, private val activity: Activity?) {
    private val gso = GoogleSignInHelper.getSignInOptions(context)
    fun showAccountDialog(name: String, email: String, photoURL: String){
        Log.d("TAG", "Entered showAccountDialog")
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.account_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.name).text = name
        dialog.findViewById<TextView>(R.id.email).text = email
        Log.d("TAG", "Photo URL: $photoURL");
        Glide.with(context)
            .load(photoURL)
            .circleCrop()
            .into(dialog.findViewById(R.id.photo))

        dialog.show()
        val close = dialog.findViewById<ImageView>(R.id.close_btn)
        close.setOnClickListener{
            dialog.dismiss()
        }

        val logout = dialog.findViewById<TextView>(R.id.logoutBtn)
        logout.setOnClickListener{
            signOut()
            if (activity != null) {
                dialog.dismiss()
                activity.finish()
                activity.startActivity(Intent(activity, Login::class.java))
            }
        }
    }

    fun showArtistDialog(artist : Artist){
        Log.d("TAG", "Entered showArtistDialog")
        val dialog = Dialog(activity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.about_artist_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.findViewById<TextView>(R.id.artist_name).text = artist.title
        dialog.findViewById<TextView>(R.id.aboutText).text = artist.text

        dialog.show()
        val url = artist.banner
        Glide.with(context)
            .load(url)
            .into(dialog.findViewById(R.id.artist_image))

        val close = dialog.findViewById<ImageView>(R.id.close_btn)
        close.setOnClickListener{
            dialog.dismiss()
        }
    }

    fun signOut(){
        FirebaseAuth.getInstance().signOut()
        GoogleSignIn.getClient(context, gso).signOut()
    }
}
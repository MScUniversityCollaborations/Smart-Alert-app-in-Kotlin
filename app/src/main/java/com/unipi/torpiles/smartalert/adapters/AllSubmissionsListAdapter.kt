package com.unipi.torpiles.smartalert.adapters

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.databinding.ItemSubmissionGlobalBinding
import com.unipi.torpiles.smartalert.models.Submission
import com.unipi.torpiles.smartalert.utils.GlideLoader
import com.unipi.torpiles.smartalert.utils.IntentUtils
import java.util.*


/**
 * A adapter class for all submissions list items.
 */
open class AllSubmissionsListAdapter(
    private val context: Context,
    private var list: ArrayList<Submission>
) : RecyclerView.Adapter<AllSubmissionsListAdapter.AllSubmissionsViewHolder>() {

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ProductsViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllSubmissionsViewHolder {
        return AllSubmissionsViewHolder(
            ItemSubmissionGlobalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: AllSubmissionsViewHolder, position: Int) {
        val model = list[position]

        holder.binding.apply {
            GlideLoader(context).loadPictureWide(model.imgUrl, imageViewPicture)
            // txtViewLocation.text = model.name
            textViewDate.text = model.dateAdded.toString()
            // Get location details from lat and longitude
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(model.lat.toDouble(), model.long.toDouble(), 1)
            val obj = addresses[0]
            val cityName: String = obj.getAddressLine(0)
            val countryName: String = obj.countryName

            textViewLocation.text = String.format(context.getString(R.string.format_location_details), countryName, cityName)
            textViewCategory.text = String.format(context.getString(R.string.format_category), model.category)
            textViewDesc.text = model.description
            textViewByUser.text = model.user.fullName

            // If submission is verified by an admin!
            if (model.accepted)
                textViewByUser.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_tick_rounded_icon, 0, 0, 0)
            else
                textViewByUser.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_cross_rounded_icon, 0, 0, 0)

            // If submission is marked as high danger!
            if (model.highDanger)
                textViewCategory.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.svg_warning_20, 0, 0, 0)
            // textViewLatLong.text = String.format(context.getString(R.string.format_location_lat_long), model.lat, model.long)
        }
        holder.binding.cardViewSingleItem.setOnClickListener { IntentUtils().goToSubmissionDetailsActivity(context, model) } // true : since we are already in submissions.
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class AllSubmissionsViewHolder(val binding: ItemSubmissionGlobalBinding) : RecyclerView.ViewHolder(binding.root)
}

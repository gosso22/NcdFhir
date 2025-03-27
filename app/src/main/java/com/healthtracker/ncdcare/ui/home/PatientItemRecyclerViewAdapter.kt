package com.healthtracker.ncdcare.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.healthtracker.ncdcare.databinding.PatientListItemViewBinding
import org.hl7.fhir.r4.model.Patient

class PatientItemRecyclerViewAdapter :
    ListAdapter<Patient, PatientItemViewHolder>(PatientItemDiffCallback()) {

    private var onItemClickListener: ((Patient) -> Unit)? = null
    fun setOnItemClickListener(listener: (Patient) -> Unit) {
        onItemClickListener = listener
    }

    class PatientItemDiffCallback : DiffUtil.ItemCallback<Patient>() {
        override fun areItemsTheSame(oldItem: Patient, newItem: Patient) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Patient,
            newItem: Patient,
        ) = oldItem.equalsDeep(newItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientItemViewHolder {
        return PatientItemViewHolder(
            PatientListItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: PatientItemViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }
}
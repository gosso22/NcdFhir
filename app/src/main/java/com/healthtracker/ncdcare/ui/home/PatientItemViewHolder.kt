package com.healthtracker.ncdcare.ui.home

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.healthtracker.ncdcare.R
import com.healthtracker.ncdcare.databinding.PatientListItemViewBinding
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient

class PatientItemViewHolder(binding: PatientListItemViewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val nameTextView: TextView = binding.name
    private val genderTextView: TextView = binding.gender
    private val cityTextView = binding.city
    private val genderIcon = binding.genderIcon

    fun bind(patientItem: Patient) {
        nameTextView.text =
            patientItem.name.firstOrNull()?.let {
                it.given.firstOrNull().toString() + " " + it.family
            }
        genderIcon.setImageResource(getGenderDrawableResource(patientItem.gender))
        genderTextView.text = patientItem.gender?.display
        cityTextView.text = patientItem.address.singleOrNull()?.city
    }

    private fun getGenderDrawableResource(patient: Enumerations.AdministrativeGender?): Int {
        return when (patient) {
            Enumerations.AdministrativeGender.MALE -> R.mipmap.ic_man
            Enumerations.AdministrativeGender.FEMALE -> R.mipmap.ic_woman
            else -> R.drawable.sidemenu_fp
        }
    }

}
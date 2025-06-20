package com.myskripsi.gokos.ui.fragment.maps

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myskripsi.gokos.databinding.FragmentMapsBinding
import com.myskripsi.gokos.ui.activity.map.MappingMapActivity

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardUnsera.setOnClickListener {
            navigateToMappingMap("Pl7iiRGC0FbENz4bnHwq", "Universitas Serang Raya")
        }

        binding.cardUniba.setOnClickListener {
            navigateToMappingMap("kc9H58uqdcNcldwF2ce8", "Universitas Bina Bangsa")
        }

        binding.cardFkip.setOnClickListener {
            navigateToMappingMap("HZKQ0kvV6NP0dLJQGdVV", "FKIP Unitirta")
        }

        binding.cardUinsmhbanten.setOnClickListener {
            navigateToMappingMap("BnNlYEeXsxnCva26gIwu", "UIN SMH Banten")
        }
    }

    private fun navigateToMappingMap(campusId: String, campusName: String) {
        val intent = Intent(requireContext(), MappingMapActivity::class.java).apply {
            putExtra(MappingMapActivity.EXTRA_CAMPUS_ID, campusId)
            putExtra(MappingMapActivity.EXTRA_CAMPUS_NAME, campusName)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
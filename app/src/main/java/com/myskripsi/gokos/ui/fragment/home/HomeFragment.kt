package com.myskripsi.gokos.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.myskripsi.gokos.databinding.FragmentHomeBinding
import com.myskripsi.gokos.ui.activity.ListKosActivity
import com.myskripsi.gokos.ui.adapter.KosAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // initialization ViewModel with Koin
    private val viewModel: HomeViewModel by viewModel()

    // adapter for RecyclerView data kos
    private lateinit var kosAdapter: KosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigateToListKosActivity()
    }

    private fun navigateToListKosActivity() {
        binding.cardViewUnsera.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "Pl7iiRGC0FbENz4bnHwq")
            }
            startActivity(intent)
        }
        binding.cardViewUniba.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "kc9H58uqdcNcldwF2ce8")
            }
            startActivity(intent)
        }
        binding.cardViewUIN.setOnClickListener {
            val intent = Intent(requireContext(), ListKosActivity::class.java).apply {
                putExtra(ListKosActivity.EXTRA_CAMPUS_ID, "BnNlYEeXsxnCva26gIwu")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
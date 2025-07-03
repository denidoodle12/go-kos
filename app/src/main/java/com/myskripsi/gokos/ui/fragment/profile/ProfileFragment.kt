package com.myskripsi.gokos.ui.fragment.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.myskripsi.gokos.R
import com.myskripsi.gokos.data.model.UserProfile
import com.myskripsi.gokos.databinding.FragmentProfileBinding
import com.myskripsi.gokos.ui.activity.auth.login.LoginActivity
import com.myskripsi.gokos.ui.activity.profile.editProfile.EditProfileActivity
import com.myskripsi.gokos.ui.activity.profile.PrivacyPolicyActivity
import com.myskripsi.gokos.ui.activity.profile.TermsConditionActivity
import com.myskripsi.gokos.ui.activity.profile.aboutApp.AboutAppActivity
import com.myskripsi.gokos.ui.activity.profile.personalData.PersonalDataActivity
import com.myskripsi.gokos.ui.fragment.customalertdialog.ConfirmationDialogFragment
import com.myskripsi.gokos.utils.Result
import org.koin.androidx.viewmodel.ext.android.viewModel

// Implementasikan listener dari dialog
class ProfileFragment : Fragment(), ConfirmationDialogFragment.ConfirmationDialogListener {
    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModel()

    private val refreshProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == 0) {
            // Muat ulang data profil untuk menampilkan perubahan
            profileViewModel.loadUserProfile()
        }
    }

    // Callback dari dialog, dipanggil saat user menekan "IYA" / tombol positif
    override fun onConfirm() {
        // Panggil fungsi logout di ViewModel
        profileViewModel.logoutUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.loadUserProfile()

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.optionAccount.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_profile_outline)
            tvMenuTitle.text = "Ubah Profile"
            tvMenuSubtitle.text = "Ubah data profile pada akun kamu"
            root.setOnClickListener {
                val intent = Intent(requireActivity(), EditProfileActivity::class.java)
                refreshProfileLauncher.launch(intent)
            }
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(requireActivity(), PersonalDataActivity::class.java)
            refreshProfileLauncher.launch(intent)
        }

        binding.optionLanguage.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_translate)
            tvMenuTitle.text = "Bahasa"
            tvMenuSubtitle.text = "Ubah ke bahasa yang kamu inginkan"
            root.setOnClickListener {
                Toast.makeText(requireContext(), "Menu Bahasa diklik", Toast.LENGTH_SHORT).show()
            }
        }

        binding.optionAboutApp.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_aboutapp)
            tvMenuTitle.text = "Tentang Aplikasi"
            tvMenuSubtitle.text = "Informasi tentang aplikasi Go-Kos"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
            }
        }

        binding.optionPrivacyPolicy.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_privacy)
            tvMenuTitle.text = "Kebijakan Privasi"
            tvMenuSubtitle.text = "Lihat kebijakan privasi Go-Kos"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), PrivacyPolicyActivity::class.java))
            }
        }

        binding.optionTerms.apply {
            ivMenuIcon.setImageResource(R.drawable.ic_rule)
            tvMenuTitle.text = "Syarat & Ketentuan"
            tvMenuSubtitle.text = "Lihat syarat & ketentuan Go-Kos"
            root.setOnClickListener {
                startActivity(Intent(requireActivity(), TermsConditionActivity::class.java))
            }
        }
    }

    private fun observeViewModel() {
        profileViewModel.userProfileState.observe(viewLifecycleOwner) { result ->
            when(result) {
                is Result.Loading -> binding.progressIndicator.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    result.data?.let { populateProfileData(it) }
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat profil: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        profileViewModel.logoutState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.btnLogout.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Logout berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToLoginScreen()
                }
                is Result.Error -> {
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnLogout.isEnabled = true
                    Toast.makeText(requireContext(), "Logout gagal: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun populateProfileData(profile: UserProfile) {
        // --- Isi data utama ---
        binding.tvUsername.text = profile.fullName
        binding.tvEmail.text = profile.email
        Glide.with(this)
            .load(profile.profileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .circleCrop()
            .into(binding.profileImage)

        // --- Isi data di CardView Data Pribadi ---
        val placeholder = "-" // Teks pengganti jika data kosong
        binding.tvGender.text = profile.gender ?: placeholder
        binding.tvDateOfBirth.text = profile.dateOfBirth ?: placeholder
        binding.tvMaritalStatus.text = profile.maritalStatus ?: placeholder
        binding.tvProfession.text = profile.profession ?: placeholder
        binding.tvProfessionName.text = profile.professionName ?: placeholder
        binding.tvEmergencyContact.text = profile.emergencyContact ?: placeholder
    }

    private fun showLogoutConfirmationDialog() {
        // Gunakan ConfirmationDialogFragment yang sudah kita buat
        val dialog = ConfirmationDialogFragment.newInstance(
            "Konfirmasi",
            "Apakah Anda yakin ingin keluar dari akun ini?",
            "IYA",
            "TIDAK"
        )
        // Karena fragment ini sudah mengimplementasikan listener,
        // dialog akan otomatis terhubung.
        dialog.show(childFragmentManager, "ConfirmLogoutDialog")
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
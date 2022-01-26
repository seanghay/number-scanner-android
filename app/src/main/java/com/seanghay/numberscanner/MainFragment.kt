package com.seanghay.numberscanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.seanghay.numberscanner.databinding.FragmentMainBinding

class MainFragment : Fragment() {

  private var _binding: FragmentMainBinding? = null
  private val binding: FragmentMainBinding get() = _binding!!

  private val requestCameraPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        findNavController().navigate(
          MainFragmentDirections.actionMainFragmentToScannerFragment()
        )
      }
      else Toast.makeText(requireContext(), "CAMERA Permission denied!", Toast.LENGTH_SHORT).show()
    }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.button.setOnClickListener {
      requestCameraPermissionLauncher.launch(
        android.Manifest.permission.CAMERA
      )
    }
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }

}
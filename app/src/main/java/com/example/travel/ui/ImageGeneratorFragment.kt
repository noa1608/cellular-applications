package com.example.travel.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel.R
import com.example.travel.adapters.ImageAdapter
import com.example.travel.repository.ImageRepository
import com.example.travel.viewmodel.ImageGeneratorViewModel

class ImageGeneratorFragment : Fragment(R.layout.fragment_image_generator) {

    private lateinit var viewModel: ImageGeneratorViewModel
    private lateinit var adapter: ImageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = ImageRepository()
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ImageGeneratorViewModel(repo) as T
            }
        })[ImageGeneratorViewModel::class.java]

        val etQuery = view.findViewById<EditText>(R.id.et_query)
        val btnGenerate = view.findViewById<Button>(R.id.btn_generate)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_images)

        adapter = ImageAdapter()
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        btnGenerate.setOnClickListener {
            val prompt = etQuery.text.toString()
            viewModel.generateImages(prompt, 2)
        }

        viewModel.images.observe(viewLifecycleOwner) { images ->
            adapter.submitList(images)
        }
    }
}

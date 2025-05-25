package org.example

import kotlinx.serialization.Serializable

@Serializable
data class ExampleConfig(val message: String = "Hello, world!")

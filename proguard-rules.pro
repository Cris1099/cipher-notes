# Keep all data classes
-keep class dev.cipher.notes.data.** { *; }

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }

# Keep Room entities
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep ViewModels
-keep class androidx.lifecycle.ViewModel { *; }

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable

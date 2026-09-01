# --- kotlinx.serialization ---------------------------------------------------
# Les serializers sont generes et referencés par reflexion sur le companion.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **.*$serializer {
    *** descriptor;
}

# --- Retrofit / OkHttp ------------------------------------------------------
# Retrofit et OkHttp embarquent leurs propres regles ; on conserve les signatures
# generiques necessaires a la resolution des types de retour suspend.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# --- Modeles exposes a la navigation type-safe ------------------------------
-keep class fr.leboncoin.**.navigation.** { *; }

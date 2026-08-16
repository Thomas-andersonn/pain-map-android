package com.example.painmap.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AnatomicalRegion(
    val displayName: String,
    val category: BodyCategory,
    val defaultX: Float = 0f,
    val defaultY: Float = 0f,
    val defaultZ: Float = 0f
) {
    HEAD("Head / Cranium", BodyCategory.HEAD_NECK, 0f, 1.7f, 0f),
    FOREHEAD("Forehead / Frontal", BodyCategory.HEAD_NECK, 0f, 1.75f, 0.1f),
    TEMPLE_LEFT("Left Temple", BodyCategory.HEAD_NECK, -0.1f, 1.72f, 0.05f),
    TEMPLE_RIGHT("Right Temple", BodyCategory.HEAD_NECK, 0.1f, 1.72f, 0.05f),
    JAW_FACIAL("Jaw / TMJ / Facial", BodyCategory.HEAD_NECK, 0f, 1.6f, 0.1f),
    NECK_CERVICAL("Neck / Cervical Spine", BodyCategory.HEAD_NECK, 0f, 1.5f, -0.05f),
    
    CHEST_THORACIC("Chest / Sternum", BodyCategory.TORSO, 0f, 1.3f, 0.12f),
    UPPER_BACK("Upper Back / Thoracic", BodyCategory.TORSO, 0f, 1.35f, -0.1f),
    LOWER_BACK_LUMBAR("Lower Back / Lumbar", BodyCategory.TORSO, 0f, 1.05f, -0.1f),
    ABDOMEN("Abdomen", BodyCategory.TORSO, 0f, 1.1f, 0.12f),
    PELVIS_HIPS("Pelvis / Sacrum", BodyCategory.TORSO, 0f, 0.95f, 0f),
    
    SHOULDER_LEFT("Left Shoulder / Deltoid", BodyCategory.UPPER_LIMB, -0.3f, 1.4f, 0f),
    SHOULDER_RIGHT("Right Shoulder / Deltoid", BodyCategory.UPPER_LIMB, 0.3f, 1.4f, 0f),
    ARM_UPPER_LEFT("Left Upper Arm / Biceps", BodyCategory.UPPER_LIMB, -0.35f, 1.25f, 0f),
    ARM_UPPER_RIGHT("Right Upper Arm / Biceps", BodyCategory.UPPER_LIMB, 0.35f, 1.25f, 0f),
    ELBOW_LEFT("Left Elbow", BodyCategory.UPPER_LIMB, -0.38f, 1.1f, -0.05f),
    ELBOW_RIGHT("Right Elbow", BodyCategory.UPPER_LIMB, 0.38f, 1.1f, -0.05f),
    FOREARM_LEFT("Left Forearm", BodyCategory.UPPER_LIMB, -0.4f, 0.95f, 0f),
    FOREARM_RIGHT("Right Forearm", BodyCategory.UPPER_LIMB, 0.4f, 0.95f, 0f),
    WRIST_HAND_LEFT("Left Wrist & Hand", BodyCategory.UPPER_LIMB, -0.45f, 0.8f, 0f),
    WRIST_HAND_RIGHT("Right Wrist & Hand", BodyCategory.UPPER_LIMB, 0.45f, 0.8f, 0f),
    
    HIP_LEFT("Left Hip / Gluteal", BodyCategory.LOWER_LIMB, -0.18f, 0.9f, 0f),
    HIP_RIGHT("Right Hip / Gluteal", BodyCategory.LOWER_LIMB, 0.18f, 0.9f, 0f),
    THIGH_QUAD_LEFT("Left Thigh / Quad", BodyCategory.LOWER_LIMB, -0.18f, 0.7f, 0.05f),
    THIGH_QUAD_RIGHT("Right Thigh / Quad", BodyCategory.LOWER_LIMB, 0.18f, 0.7f, 0.05f),
    KNEE_LEFT("Left Knee", BodyCategory.LOWER_LIMB, -0.18f, 0.5f, 0.06f),
    KNEE_RIGHT("Right Knee", BodyCategory.LOWER_LIMB, 0.18f, 0.5f, 0.06f),
    CALF_SHIN_LEFT("Left Calf / Shin", BodyCategory.LOWER_LIMB, -0.18f, 0.3f, 0f),
    CALF_SHIN_RIGHT("Right Calf / Shin", BodyCategory.LOWER_LIMB, 0.18f, 0.3f, 0f),
    ANKLE_FOOT_LEFT("Left Ankle & Foot", BodyCategory.LOWER_LIMB, -0.18f, 0.08f, 0.05f),
    ANKLE_FOOT_RIGHT("Right Ankle & Foot", BodyCategory.LOWER_LIMB, 0.18f, 0.08f, 0.05f)
}

@Serializable
enum class BodyCategory(val label: String) {
    HEAD_NECK("Head & Neck"),
    TORSO("Torso & Spine"),
    UPPER_LIMB("Upper Limbs & Arms"),
    LOWER_LIMB("Lower Limbs & Legs")
}

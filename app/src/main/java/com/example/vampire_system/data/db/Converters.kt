package com.example.vampire_system.data.db

import androidx.room.TypeConverter
import com.example.vampire_system.data.model.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

object Converters {

    // Enums (store as String)
    @TypeConverter fun fromAbilityGroup(v: AbilityGroup?): String? = v?.name
    @TypeConverter fun toAbilityGroup(s: String?): AbilityGroup? = s?.let { AbilityGroup.valueOf(it) }

    @TypeConverter fun fromTaskCategory(v: TaskCategory?): String? = v?.name
    @TypeConverter fun toTaskCategory(s: String?): TaskCategory? = s?.let { TaskCategory.valueOf(it) }

    @TypeConverter fun fromQuestKind(v: QuestKind?): String? = v?.name
    @TypeConverter fun toQuestKind(s: String?): QuestKind? = s?.let { QuestKind.valueOf(it) }

    @TypeConverter fun fromQuestStatus(v: QuestStatus?): String? = v?.name
    @TypeConverter fun toQuestStatus(s: String?): QuestStatus? = s?.let { QuestStatus.valueOf(it) }

    @TypeConverter fun fromEvidenceKind(v: EvidenceKind?): String? = v?.name
    @TypeConverter fun toEvidenceKind(s: String?): EvidenceKind? = s?.let { EvidenceKind.valueOf(it) }

    @TypeConverter fun fromStreakTier(v: StreakTier?): String? = v?.name
    @TypeConverter fun toStreakTier(s: String?): StreakTier? = s?.let { StreakTier.valueOf(it) }

    // Lists/Sets/Maps as JSON
    @TypeConverter fun fromStringList(list: List<String>?): String? =
        list?.let { json.encodeToString(ListSerializer(String.serializer()), it) }
    @TypeConverter fun toStringList(s: String?): List<String> =
        s?.let { json.decodeFromString(ListSerializer(String.serializer()), it) } ?: emptyList()

    @TypeConverter fun fromEvidenceKindSet(set: Set<EvidenceKind>?): String? =
        set?.let { json.encodeToString(SetSerializer(EvidenceKind.serializer()), it) }
    @TypeConverter fun toEvidenceKindSet(s: String?): Set<EvidenceKind> =
        s?.let { json.decodeFromString(SetSerializer(EvidenceKind.serializer()), it) } ?: emptySet()

    @TypeConverter fun fromStringMap(map: Map<String,String>?): String? =
        map?.let { json.encodeToString(MapSerializer(String.serializer(), String.serializer()), it) }
    @TypeConverter fun toStringMap(s: String?): Map<String,String> =
        s?.let { json.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it) } ?: emptyMap()

    // Polymorphic + complex as JSON
    @TypeConverter fun fromXpRule(rule: XpRule?): String? =
        rule?.let { json.encodeToString(XpRule.serializer(), it) }
    @TypeConverter fun toXpRule(s: String?): XpRule? =
        s?.let { json.decodeFromString(XpRule.serializer(), it) }

    @TypeConverter fun fromSeriousness(v: Seriousness?): String? =
        v?.let { json.encodeToString(Seriousness.serializer(), it) }
    @TypeConverter fun toSeriousness(s: String?): Seriousness? =
        s?.let { json.decodeFromString(Seriousness.serializer(), it) }
}



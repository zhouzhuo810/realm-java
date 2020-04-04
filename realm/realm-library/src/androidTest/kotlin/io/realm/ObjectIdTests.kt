package io.realm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.realm.TestHelper.generateObjectIdHexString
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import io.realm.exceptions.RealmException
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.bson.types.ObjectId
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

open class ObjectIdPrimaryKeyRequired
 : RealmObject() {
    @field:PrimaryKey
    @field:Required
    var id : ObjectId? = null
    var name : String = ""

}

open class ObjectIdPrimaryKeyNotRequired
    : RealmObject() {
    @field:PrimaryKey
    var id : ObjectId? = null
    var name : String = ""

}

@RunWith(AndroidJUnit4::class)
class ObjectIdTests {
    private lateinit var realmConfiguration: RealmConfiguration
    private lateinit var realm: Realm

    @Rule
    @JvmField val folder = TemporaryFolder()

    init {
        Realm.init(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Before
    fun setUp() {
        realmConfiguration = RealmConfiguration
                .Builder(InstrumentationRegistry.getInstrumentation().targetContext)
                .directory(folder.newFolder())
                .schema(ObjectIdPrimaryKeyRequired::class.java, ObjectIdPrimaryKeyNotRequired::class.java).build()
        realm = Realm.getInstance(realmConfiguration)
    }

    @After
    fun tearDown() {
        realm.close()
    }

    @Test
    fun requiredPK() {
        realm.beginTransaction()
        try {
            realm.createObject<ObjectIdPrimaryKeyRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        val obj = realm.createObject<ObjectIdPrimaryKeyRequired>(ObjectId(generateObjectIdHexString(42)))
        obj.name = "foo"

        realm.commitTransaction()

        // FIXME re-enable when Core add query support
        val result = realm.where<ObjectIdPrimaryKeyRequired>().equalTo("id", ObjectId(generateObjectIdHexString(42))).findFirst()
        assertNotNull(result)
        assertEquals("foo", result?.name)
    }

    @Test
    fun nullablePK() {
        try {
            realm.createObject<ObjectIdPrimaryKeyNotRequired>()
            fail()
        } catch (ignore: RealmException) {
        }

        realm.beginTransaction()
        val obj = realm.createObject<ObjectIdPrimaryKeyNotRequired>(null)
        obj.name = "foo"
        realm.commitTransaction()

        val result = realm.where<ObjectIdPrimaryKeyNotRequired>().equalTo("id", null as ObjectId?).findFirst()
        assertNotNull(result)
        assertEquals("foo", result?.name)
    }
}
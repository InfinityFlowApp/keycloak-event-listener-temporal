package app.infinityflow.keycloak.events.tests

import org.keycloak.Config.Scope

class TestScope : Scope {

    private val _map: Map<String, String> = mapOf(
        "server" to "localhost:7233",
        "task-queue" to "default",
        "namespace" to "default",
        "mtls-cert-file" to "",
        "mtls-key-file" to "",
        "mtls-override-authority" to "",
    )

    override fun get(p0: String?): String {
        return _map[p0] as String
    }

    override fun get(p0: String?, p1: String?): String {
        return _map.getOrDefault(p0, p1) as String
    }

    override fun getArray(p0: String?): Array<String> {
        return _map.values.filter { a -> a == p0 }.toTypedArray()
    }

    override fun getInt(p0: String?): Int {
        return _map[p0]!!.toInt(10)
    }

    override fun getInt(p0: String?, p1: Int?): Int {
        if (_map.containsKey(p0))
            return _map[p0]!!.toInt(10)
        return p1 as Int
    }

    override fun getLong(p0: String?): Long {
        return _map[p0]!!.toLong(10)
    }

    override fun getLong(p0: String?, p1: Long?): Long {
        if (_map.containsKey(p0))
            return _map[p0]!!.toLong(10)
        return p1 as Long
    }

    override fun getBoolean(p0: String?): Boolean {
        return _map[p0].toBoolean()
    }

    override fun getBoolean(p0: String?, p1: Boolean?): Boolean {
        if (_map.containsKey(p0))
            return _map[p0].toBoolean()
        return p1 as Boolean
    }

    override fun scope(vararg p0: String?): Scope {
        return TestScope()
    }

    override fun getPropertyNames(): MutableSet<String> {
        return _map.keys.toMutableSet()
    }
}

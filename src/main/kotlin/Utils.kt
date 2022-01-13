class Utils {
    fun check(clientId: Int, clientList: MutableList<Client>): Boolean {
        var flag = false
        for (client in clientList) {
            if (client.id == clientId) flag = true
        }
        return flag
    }
}

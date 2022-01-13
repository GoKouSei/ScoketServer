import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket

const val heartbeatVal = 0
const val chatRoom = 1
const val chat = 2
private val privateChatServer = ServerSocket(12535)
private val chatRoomServer = ServerSocket(12636)
fun main(args: Array<String>) {
    ChatRoomServer(chatRoomServer).start()
    println("开始服务器监听......")
}

data class Message(val clientId: Int, val targetId: Int, val message: String) : Serializable
data class Client(val id: Int, val out: OutputStream, val client: Socket)
class ChatRoomServer(private val server: ServerSocket) {
    private val messageQueue = mutableListOf<Message>()
    private val clientList = mutableMapOf<Int, Client>()


    fun start() {
        Write().start()
        while (true) {
            val socket = server.accept()
            Read(socket).start()
        }
    }

    inner class Read(private val socket: Socket) : Thread() {
        override fun run() {
            var type: Int
            try {
                while (true) {
                    val input = DataInputStream(socket.getInputStream())
                    type = input.readInt()
                    when (type) {
                        heartbeatVal -> println("心跳")
                        else -> {
                            val clientId = input.readInt()
                            val targetId = input.readInt()
                            val message = input.readUTF()
                            clientList[clientId] = Client(clientId, socket.getOutputStream(), socket)
                            messageQueue.add(Message(clientId, targetId, message))
                            println("收到的信息: $message")
                            println("客户端ID: $clientId")
                            println("目标客户端ID: $targetId")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    inner class Write : Thread() {
        override fun run() {
            println("开始监听信息")
            while (true) {
                try {
                    println("message长度:${messageQueue.count()}")
                    println("client长度:${clientList.count()}")
                    val temp = mutableListOf<Message>()
                    for (queue in messageQueue) {
                        for (client in clientList.keys) {
                            if (clientList[client]!!.client.isConnected) {
                                if (queue.clientId != client) {
                                    val output = DataOutputStream(clientList[client]!!.out)
                                    val message = queue.message
                                    output.writeUTF(message)
                                    println("发送的信息:$message")
                                    output.flush()
                                    temp.add(queue)
                                }
                            }
                        }
                    }
                    messageQueue.removeAll(temp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                sleep(1000)
            }
        }
    }
}
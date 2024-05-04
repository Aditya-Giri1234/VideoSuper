package com.example.videosuper.util

object Constant {

    enum class DataTypeModel {
        Offer,
        Answer,
        IceCandidate
    }

    enum class CallStatus {
        CreateCall,
        RequestCall,
        AcceptCall,
        SendRoomId,
        AcceptRoomId,
        CreateRoom,
        JoinRoom,
        RejectCall,
        Disconnect,
        CancelCall
    }

    enum class UserStatus {
        Free,
        Busy,
        NotAvailable,
        RejectByUser
    }

}
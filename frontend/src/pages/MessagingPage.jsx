import axios from "axios";
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useUser } from "../context/UserContext";

const MessagingPage = () => {
  const navigate = useNavigate();

  const { listingId } = useParams();

  const [participantUsername, setParticipantUsername] = useState();

  const [conversations, setConversations] = useState();

  const user = useAuthStore((state) => state.user);

  if (user == null) {
    navigate("/");
  }

  const [response, setResponse] = useState();

  const [sendRequest, SetSendRequest] = useState({
    relatedListingId: listingId,
  });

  async function fetchConversations() {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/messages/conversations`,
        {
          withCredentials: true,
        },
      );
      if (res.status == 200) {
        setConversations(res.data);

        console.log(res.data);
      }
    } catch (error) {
      console.log(error);
    }
  }

  async function fetchMessages(conversationId) {
    try {
      const res = await axios.get(
        `http://localhost:8080/api/messages/conversation/${listingId}`,
        {
          withCredentials: true,
        },
      );
      if (res.status == 200) {
        setMessages(res.data.messagePage.content);
        setParticipantUsername(
          user.username == res.data.conversationDTO.initiatorUsername
            ? res.data.conversationDTO.participantUsername
            : res.data.conversationDTO.initiatorUsername,
        );
        console.log(res.data);
      }
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    fetchConversations();
  }, []);

  const [messages, setMessages] = useState([]);

  const [newMessage, setNewMessage] = useState("");

  const handleSend = async () => {
    if (newMessage == "") {
      return;
    }

    SetSendRequest((prevData) => ({
      ...prevData,
      content: newMessage,
    }));

    try {
      const res = await axios.post(
        "http://localhost:8080/api/messages",
        {
          relatedListingId: sendRequest.relatedListingId,
          content: newMessage,
        },
        { withCredentials: true },
      );

      console.log(res.data);
      if (res.status == 200) {
        setResponse(res.data);
      }
    } catch (error) {
      console.log(error);
    }

    setNewMessage("");
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setNewMessage((prevData) => ({
      ...prevData,
      [name]: value,
    }));
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="flex flex-col h-screen w-screen max-w-[900px] bg-black">
      {/* Header */}
      <div className="bg-white border-b px-6 py-4 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-linear-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center text-white font-semibold">
            A
          </div>
          <div>
            <h2 className="font-semibold text-gray-900 text-left">
              {participantUsername}
            </h2>
            <p className="text-sm text-gray-500">Active now</p>
          </div>
        </div>
        <div></div>
      </div>
      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
        {messages.map((message) => (
          <div
            key={message.id}
            className={`flex ${
              message.senderUsername === user.username
                ? "justify-end"
                : "justify-start"
            }`}
          >
            <div
              className={`max-w-md ${
                message.senderUsername === user.username
                  ? "bg-blue-500 text-white"
                  : "bg-white text-gray-900 border border-gray-200"
              } rounded-2xl px-4 py-2.5 shadow-sm`}
            >
              {message.senderUsername !== user.username && (
                <p className="text-xs font-semibold mb-1 text-gray-600">
                  {message.senderUsername}
                </p>
              )}
              <p className="text-sm leading-relaxed">{message.content}</p>
              <p
                className={`text-xs mt-1 ${
                  message.senderUsername === user.username
                    ? "text-blue-100"
                    : "text-gray-500"
                }`}
              >
                {new Date(message.timestamp).toLocaleTimeString("en-US", {
                  hour: "numeric",
                  minute: "2-digit",
                })}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Input Area */}
      <div className="bg-white border-t px-6 py-4 text-black">
        <div className="flex gap-3 items-end">
          <div className="flex-1">
            <textarea
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder="Type your message..."
              className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              rows="1"
            />
          </div>
          <button
            onClick={handleSend}
            className="bg-blue-500 hover:bg-blue-600 text-white px-6 py-3 rounded-xl font-medium transition-colors"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
};

export default MessagingPage;

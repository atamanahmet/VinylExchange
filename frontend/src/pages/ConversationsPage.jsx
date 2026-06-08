import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuthStore } from "../stores/authStore";
import { useMessagingStore } from "../stores/messagingStore";
import { useUIStore } from "../stores/uiStore";
import { useListingStore } from "../stores/listingStore";

export default function ConversationsPage() {
  const navigate = useNavigate();
  const { listingId } = useParams();

  const user = useAuthStore((state) => state.user);
  const setOpenLogin = useUIStore((state) => state.setOpenLogin);

  const setActiveConvoId = useMessagingStore((state) => state.setActiveConvoId);
  const activeConvoId = useMessagingStore((state) => state.activeConvoId);
  const activeConversation = useMessagingStore(
    (state) => state.activeConversation,
  );
  const setActiveConversation = useMessagingStore(
    (state) => state.setActiveConversation,
  );
  const sendMessage = useMessagingStore((state) => state.sendMessage);
  const fetchConversations = useMessagingStore(
    (state) => state.fetchConversations,
  );
  const fetchMessages = useMessagingStore((state) => state.fetchMessages);
  const deleteAllConversations = useMessagingStore(
    (state) => state.deleteAllConversations,
  );
  const deleteConversation = useMessagingStore(
    (state) => state.deleteConversation,
  );
  const conversations = useMessagingStore((state) => state.conversations);

  const fetchListing = useListingStore((state) => state.fetchListing);
  const currentListing = useListingStore((state) => state.currentListing);

  const [newMessage, setNewMessage] = useState("");
  const [participantUsername, setParticipantUsername] = useState("");

  /** confirm delete popup state */
  const [deleteTarget, setDeleteTarget] = useState(null); // { id, type: 'one' | 'all' }

  useEffect(() => {
    if (!user) {
      navigate("/");
      return;
    }
    fetchConversations();
  }, [user]);

  useEffect(() => {
    if (!activeConvoId) return;
    fetchMessages(activeConvoId);
  }, [activeConvoId]);

  /** fetch listing info when active conversation changes */
  useEffect(() => {
    if (!activeConversation?.conversation?.relatedListingId) return;
    fetchListing(activeConversation.conversation.relatedListingId);
  }, [activeConversation]);

  /** resolve which username is the other party */
  useEffect(() => {
    if (!activeConversation?.conversation || !user) return;
    const convo = activeConversation.conversation;
    setParticipantUsername(
      convo.initiatorUsername === user.username
        ? convo.participantUsername
        : convo.initiatorUsername,
    );
  }, [activeConversation, user]);

  const handleSend = async () => {
    if (!activeConversation?.conversation?.id || !newMessage.trim()) return;
    try {
      await sendMessage(activeConversation, newMessage);
      await fetchMessages(activeConversation.conversation.id);
    } catch (error) {
      console.log(error);
    }
    setNewMessage("");
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    if (deleteTarget.type === "all") {
      await deleteAllConversations();
    } else {
      await deleteConversation(deleteTarget.id);
    }
    setDeleteTarget(null);
  };

  return (
    <div className="flex h-[calc(100vh-64px)] max-w-7xl mx-auto bg-black">
      {/* delete confirm modal */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
          <div className="bg-neutral-900 border border-neutral-700 rounded-lg p-6 w-80 shadow-xl">
            <h3 className="text-white font-semibold text-lg mb-2">
              {deleteTarget.type === "all"
                ? "Delete all conversations?"
                : "Delete this conversation?"}
            </h3>
            <p className="text-gray-400 text-sm mb-6">
              This action cannot be undone.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setDeleteTarget(null)}
                className="px-4 py-2 rounded-md text-sm font-medium text-gray-300 border border-neutral-600 hover:bg-neutral-800"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteConfirm}
                className="px-4 py-2 rounded-md text-sm font-medium text-white bg-red-600 hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* sidebar */}
      <div className="w-2/8 bg-neutral-primary border-r border-neutral-secondary flex flex-col">
        <header className="py-4 px-4 border-b border-neutral-secondary bg-accent-primary shrink-0">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-semibold text-white">Conversations</h2>
            <button
              className="bg-red-500 hover:bg-red-600 text-white rounded p-0.5 text-sm font-medium transition-colors"
              onClick={() => setDeleteTarget({ type: "all" })}
            >
              Delete All
            </button>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto">
          {conversations && conversations.length > 0 ? (
            conversations.map((convo) => (
              <div
                key={convo.id}
                className="group px-3 py-2 border-b border-neutral-secondary hover:bg-neutral-secondary-soft transition-colors"
              >
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setActiveConvoId(convo.id)}
                    className="flex items-center gap-3 flex-1 min-w-0 text-left"
                  >
                    <div className="w-12 h-12 bg-neutral-secondary-medium rounded-full shrink-0 overflow-hidden">
                      <img
                        src="https://placehold.co/200x/ffa8e4/ffffff.svg?text=ʕ•́ᴥ•̀ʔ&font=Lato"
                        alt="User Avatar"
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h2 className="text-base font-semibold text-heading truncate">
                        {user && convo.participantUsername === user.username
                          ? convo.initiatorUsername
                          : convo.participantUsername}
                      </h2>
                      <p className="text-sm text-body truncate">
                        {convo.lastMessagePreview}
                      </p>
                    </div>
                  </button>

                  {/* per-conversation delete */}
                  <button
                    onClick={() =>
                      setDeleteTarget({ id: convo.id, type: "one" })
                    }
                    className="opacity-0 group-hover:opacity-100 transition-opacity shrink-0 text-gray-500 hover:text-red-500 p-1"
                    aria-label="Delete conversation"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                  </button>
                </div>
              </div>
            ))
          ) : (
            <div className="flex items-center justify-center h-full">
              <p className="text-body">No conversations yet</p>
            </div>
          )}
        </div>
      </div>

      {/* main chat area */}
      <div className="flex-1 flex flex-col bg-neutral-primary">
        {user && activeConversation?.messages ? (
          <>
            {/* chat header — shows participant + related listing */}
            <header className="bg-neutral-primary border-b border-neutral-secondary p-3 flex items-center justify-between shrink-0">
              <div className="flex items-center gap-3">
                <img
                  src="./placeholder.png"
                  alt=""
                  className="h-10 w-10 rounded-full bg-neutral-secondary-medium object-cover"
                />
                <h2 className="text-lg font-semibold text-heading">
                  {participantUsername}
                </h2>
              </div>

              {/* related listing link */}
              {currentListing && (
                <button
                  onClick={() => navigate(`/listing/${currentListing.id}`)}
                  className="flex items-center gap-2 text-sm text-amber-500 hover:text-amber-400 border border-neutral-700 rounded-md px-3 py-1.5 hover:bg-neutral-800 transition-colors"
                >
                  {currentListing.imagePaths?.[0] && (
                    <img
                      src={currentListing.imagePaths[0]}
                      alt=""
                      className="w-6 h-6 rounded object-cover"
                    />
                  )}
                  <span className="truncate text-xs text-gray-400">
                    {[
                      currentListing.artistName,
                      currentListing.year,
                      currentListing.format,
                    ]
                      .filter(Boolean)
                      .join(" · ")}
                  </span>
                  <svg
                    className="w-4 h-4 shrink-0"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                    />
                  </svg>
                </button>
              )}
            </header>

            {/* messages */}
            <div className="flex-1 overflow-y-auto px-4 py-4 space-y-3">
              {activeConversation.messages.map((message) => (
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
                        ? "bg-amber-600 text-white"
                        : "bg-indigo-950 text-white"
                    } rounded-2xl px-4 py-2.5 shadow-sm`}
                  >
                    {message.senderUsername !== user.username && (
                      <p className="text-xs text-left font-semibold mb-1 text-body">
                        {message.senderUsername}
                      </p>
                    )}
                    <p className="text-sm text-left leading-relaxed wrap-break-words">
                      {message.content}
                    </p>
                    <p
                      className={`text-xs mt-1 ${
                        message.senderUsername === user.username
                          ? "text-white/80 text-right"
                          : "text-body text-left"
                      }`}
                    >
                      {new Date(message.timestamp).toLocaleTimeString("tr-TR", {
                        hour: "numeric",
                        minute: "2-digit",
                      })}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {/* input */}
            <footer className="bg-neutral-primary border-t border-neutral-secondary p-4 shrink-0">
              <div className="flex items-end gap-2">
                <textarea
                  placeholder="Type a message..."
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyDown={handleKeyPress}
                  rows="1"
                  className="flex-1 p-2.5 rounded-base border border-neutral-tertiary text-heading bg-neutral-primary focus:outline-none focus:ring-2 focus:ring-accent-primary focus:border-accent-primary placeholder-body resize-none min-h-[42px] max-h-32"
                  style={{ overflowY: "auto" }}
                />
                <button
                  className="bg-accent-primary hover:bg-accent-primary-dark text-white px-4 py-2.5 rounded-base font-medium transition-colors shrink-0"
                  onClick={handleSend}
                >
                  Send
                </button>
              </div>
            </footer>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <p className="text-body text-lg">
              Select a conversation to start messaging
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

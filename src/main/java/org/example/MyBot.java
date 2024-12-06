package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class MyBot extends TelegramLongPollingBot {
    private final Map<Long, List<String>> goals = new HashMap<>();
    private final Map<Long, String> awaitingGoalAction = new HashMap<>();
    private final Map<Long, Boolean> userRegistered = new HashMap<>();
    private final Map<Long, String> userNames = new HashMap<>();

    @Override
    public String getBotUsername() {
        return "@qwaszx124578_bot";
    }

    @Override
    public String getBotToken() {
        return "7682683470:AAGZZZ6RMF_f_qVw8NBCUotcxPBc2JY7FnE";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();


            if (!userRegistered.containsKey(chatId) || !userRegistered.get(chatId)) {
                if (messageText.equals("/start")) {
                    sendMessage(chatId, "Iltimos, ismingizni va familiyangizni kiriting:");
                    userRegistered.put(chatId, false);
                } else if (!userRegistered.get(chatId)) {
                    String[] nameParts = messageText.split(" ");
                    if (nameParts.length == 2) {
                        String fullName = nameParts[0] + " " + nameParts[1];
                        userNames.put(chatId, fullName);
                        sendMessage(chatId, "Ro'yxatdan o'tkazildi! Xush kelibsiz, " + fullName);
                        userRegistered.put(chatId, true);
                        sendMessage(chatId, "Buyruqlar ro'yxati:\n" +
                                "/add_goal - Yangi maqsad qo'shish\n" +
                                "/my_goals - Maqsadlaringizni ko'rish\n" +
                                "/mark_completed - Maqsadni bajarilgan deb belgilash\n" +
                                "/delete_goal - Maqsadni o'chirish\n" +
                                "/update_goal - Maqsadni yangilash");
                    } else {
                        sendMessage(chatId, "Iltimos, ismingizni va familiyangizni to'g'ri kiriting.");
                    }
                }
                return;
            }

            switch (messageText) {
                case "/start":
                    sendMessage(chatId, "Siz ro'yxatdan o'tgansiz. Buyruqlar ro'yxati:\n" +
                            "/add_goal - Yangi maqsad qo'shish\n" +
                            "/my_goals - Maqsadlaringizni ko'rish\n" +
                            "/mark_completed - Maqsadni bajarilgan deb belgilash\n" +
                            "/delete_goal - Maqsadni o'chirish\n" +
                            "/update_goal - Maqsadni yangilash");
                    break;

                case "/add_goal":
                    sendMessage(chatId, "Iltimos, maqsadingizni kiriting:");
                    awaitingGoalAction.put(chatId, "adding");
                    goals.putIfAbsent(chatId, new ArrayList<>());
                    break;

                case "/my_goals":
                    List<String> userGoals = goals.get(chatId);
                    if (userGoals != null && !userGoals.isEmpty()) {
                        StringBuilder goalList = new StringBuilder("Sizning maqsadlaringiz:\n");
                        for (int i = 0; i < userGoals.size(); i++) {
                            goalList.append((i + 1)).append("- ").append(userGoals.get(i)).append("\n");
                        }
                        sendMessage(chatId, goalList.toString());
                    } else {
                        sendMessage(chatId, "Sizda hali maqsad qo'shilmagan.");
                    }
                    break;

                case "/mark_completed":
                    handleGoalAction(chatId, "marking", "bajarilgan deb belgilash");
                    break;

                case "/delete_goal":
                    handleGoalAction(chatId, "deleting", "o'chirish");
                    break;

                case "/update_goal":
                    handleGoalAction(chatId, "updating", "yangilash");
                    break;

                default:
                    handleDefaultMessage(chatId, messageText);
                    break;
            }
        }
    }

    private void handleGoalAction(long chatId, String action, String actionName) {
        List<String> userGoals = goals.get(chatId);
        if (userGoals != null && !userGoals.isEmpty()) {
            StringBuilder goalList = new StringBuilder("Iltimos, " + actionName + " uchun maqsad raqamini kiriting:\n");
            for (int i = 0; i < userGoals.size(); i++) {
                goalList.append((i + 1)).append("- ").append(userGoals.get(i)).append("\n");
            }
            sendMessage(chatId, goalList.toString());
            awaitingGoalAction.put(chatId, action);
        } else {
            sendMessage(chatId, "Sizda hali maqsadlar mavjud emas.");
        }
    }

    private void handleDefaultMessage(long chatId, String messageText) {
        String action = awaitingGoalAction.get(chatId);

        if ("adding".equals(action)) {
            goals.get(chatId).add(messageText);
            sendMessage(chatId, "Maqsadingiz saqlandi: " + messageText);
            awaitingGoalAction.remove(chatId);
        } else if (action != null) {
            try {
                int goalIndex = Integer.parseInt(messageText) - 1;
                List<String> userGoals = goals.get(chatId);

                if (goalIndex >= 0 && goalIndex < userGoals.size()) {
                    if ("marking".equals(action)) {
                        userGoals.remove(goalIndex);
                        sendMessage(chatId, "Maqsad bajarilgan deb belgilandi va o'chirildi.");
                    } else if ("deleting".equals(action)) {
                        userGoals.remove(goalIndex);
                        sendMessage(chatId, "Maqsad o'chirildi.");
                    } else if ("updating".equals(action)) {
                        sendMessage(chatId, "Iltimos, yangi maqsadni kiriting:");
                        awaitingGoalAction.put(chatId, "updating:" + goalIndex);
                    }
                } else {
                    sendMessage(chatId, "Noto'g'ri maqsad raqami.");
                }
            } catch (NumberFormatException e) {
                if (action.startsWith("updating:")) {
                    String[] parts = action.split(":");
                    if (parts.length == 2) {
                        int indexToUpdate = Integer.parseInt(parts[1]);
                        List<String> userGoals = goals.get(chatId);
                        if (indexToUpdate >= 0 && indexToUpdate < userGoals.size()) {
                            userGoals.set(indexToUpdate, messageText);
                            sendMessage(chatId, "Maqsad yangilandi: " + messageText);
                            awaitingGoalAction.remove(chatId);
                        }
                    }
                }
                sendMessage(chatId, "Iltimos, faqat raqam yoki to'g'ri ma'lumot kiriting.");
            }
        } else {
            sendMessage(chatId, "Noto'g'ri ma'lumot kiritildi. Iltimos, faqat maqsadingizni kiriting.");
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

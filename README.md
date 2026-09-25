# 🤖 SanBot Human Interactive Promotional Robot

> **An intelligent human–robot interaction platform designed for corporate exhibitions, promotional events, and customer engagement.**

**SanBotRobot-HumanInteractive-App** is an interactive Android-based robotic application developed for **SanBot S1-B2** to create engaging human–robot experiences in corporate and promotional environments.

The system combines **AI-powered conversational interaction, human detection, gesture recognition, hand movement, and head movement** to allow the robot to naturally interact with visitors and communicate company-related information.

The platform is designed to transform a traditional promotional display into an **interactive robotic brand ambassador** capable of welcoming visitors, answering questions, presenting company information, and creating memorable exhibition experiences.

---

## ✨ Key Features

### 💬 AI Conversational Chatbot

Visitors can communicate with the robot using natural language.

The chatbot can provide information such as:

* 🏢 Company overview
* 📋 Products and services
* 📊 Company information
* 📍 Locations and contact information
* 🎯 Promotional campaigns
* ❓ Frequently asked questions
* 💡 Custom company-specific information

Instead of simply displaying static information, the robot can **actively communicate with visitors and respond to their questions.**

---

### 👤 Human Detection

The application is designed to detect the presence of people and initiate interactive experiences.

Possible interactions include:

* Detecting approaching visitors
* Initiating greetings
* Starting conversations
* Responding to nearby people
* Creating an interactive exhibition experience

This enables the robot to function as a **digital receptionist and promotional assistant**.

---

### 🤖 Robotic Hand Movements

The application integrates robot movement controls to make conversations more expressive.

Supported interactions can include:

* 👋 Hand waving
* 🙋 Hand raising
* 🫱 Gesture-based interaction
* 👋 Greeting movements
* Custom robotic gestures

These movements help make the robot feel more natural and engaging during conversations.

---

### 🗣️ Head Movement

The robot can perform head movements during interactions to create a more human-like communication experience.

Examples include:

* Looking toward visitors
* Head movement during conversations
* Greeting gestures
* Interactive reactions
* Attention-oriented movements

Combining speech with physical movement creates a more immersive **human–robot interaction (HRI)** experience.

---

## 🎯 Intended Use Cases

The platform is primarily designed for **corporate promotional and customer-engagement environments**.

### 🏢 Corporate Exhibitions

The robot can act as an interactive company representative at exhibitions and trade shows.

Visitors can ask questions about the company and receive information directly from the robot.

### 🎪 Promotional Events

Companies can use the robot to attract visitors and create an engaging promotional experience.

### 🏬 Brand Activations

The system can be customized to represent a company's brand, products, and services.

### 🤝 Customer Engagement

The robot can provide an interactive first point of contact for customers and visitors.

### 🧑‍💼 Reception & Information

The platform can potentially operate as a robotic receptionist or information assistant in corporate environments.

---

## 🧠 System Concept

The overall interaction can be represented as:

```text
                ┌──────────────────────┐
                │       Visitor        │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │   Human Detection    │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │  Robot Interaction   │
                │  Greeting / Gesture  │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │   AI Chatbot / NLP   │
                └──────────┬───────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Company Information  │
                │ Products / Services  │
                │ FAQs / Promotions   │
                └──────────┬───────────┘
                           │
                           ▼
             ┌────────────────────────────┐
             │     Robotic Response      │
             │                            │
             │  🔊 Voice                 │
             │  🤖 Head Movement         │
             │  👋 Hand Movement         │
             └────────────────────────────┘
```

---

## 🛠️ Technology Stack

| Technology             | Purpose                         |
| ---------------------- | ------------------------------- |
| **Android**            | Robot application platform      |
| **Java / Kotlin**      | Application development         |
| **SanBot Robot SDK**   | Robot hardware interaction      |
| **AI / LLM**           | Conversational intelligence     |
| **Speech Recognition** | Understanding visitor questions |
| **Text-to-Speech**     | Robot voice responses           |
| **Human Detection**    | Visitor detection               |
| **Android UI**         | Interactive user interface      |
| **REST APIs**          | External service communication  |

> The exact technologies and SDK components may vary depending on the deployed version of the application.

---

## 🤖 Robot Interaction

The application connects software intelligence with the physical capabilities of the SanBot robot.

### Software

```text
AI Chatbot
     ↓
Conversation Processing
     ↓
Company Knowledge
     ↓
Response Generation
     ↓
Voice Output
```

### Physical Interaction

```text
Visitor Detection
       ↓
   Greeting
       ↓
Head Movement
       ↓
Hand Gesture
       ↓
Voice Response
       ↓
Continuous Interaction
```

This combination creates an interactive experience rather than a conventional information kiosk.

---

## 🏢 Company-Specific Customization

One of the main purposes of this platform is to allow the robot to be configured for different companies and events.

The chatbot can be adapted with company-specific knowledge including:

```text
Company
├── About Us
├── Products
├── Services
├── Locations
├── Contact Information
├── Events
├── Promotions
├── Frequently Asked Questions
└── Custom Visitor Questions
```

This makes the application suitable for **multiple corporate environments and promotional campaigns**.

---

## 🎪 Exhibition Experience

A typical visitor interaction could follow this flow:

```text
Visitor approaches robot
          ↓
    Human detected
          ↓
      Robot greets
          ↓
 Visitor asks a question
          ↓
    AI processes request
          ↓
 Robot provides company information
          ↓
 Head / hand movement
          ↓
   Continue conversation
```

The goal is to make the robot function as an **interactive company representative** rather than a static exhibition device.

---

## 📱 Application Architecture

A simplified architecture of the system:

```text
┌──────────────────────────────────────────┐
│              Visitor                     │
└────────────────────┬─────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│          SanBot Hardware Layer            │
│                                          │
│  Camera │ Microphone │ Speaker │ Motors │
└────────────────────┬─────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│          Android Application             │
│                                          │
│  UI │ Human Detection │ Robot Control   │
└────────────────────┬─────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────┐
│          Conversation Layer              │
│                                          │
│ Speech → AI → Company Knowledge → Voice │
└──────────────────────────────────────────┘
```

---

## 🚀 Project Goals

The project focuses on combining **Artificial Intelligence, Robotics, and Human–Computer Interaction** to create a practical real-world robotic application.

### Main objectives

* Create natural human–robot conversations
* Provide company information through voice interaction
* Improve visitor engagement at exhibitions
* Use robotic gestures to make interactions more expressive
* Detect and respond to nearby visitors
* Create a customizable platform for corporate promotion
* Demonstrate practical integration between AI software and physical robotics

---

## 🔮 Future Enhancements

Potential future improvements include:

* [ ] Multi-language conversations
* [ ] Advanced facial recognition
* [ ] Emotion-aware interactions
* [ ] Visitor analytics
* [ ] Personalized greetings
* [ ] Automated exhibition navigation
* [ ] QR-code interaction
* [ ] Product recommendation system
* [ ] Lead collection and visitor registration
* [ ] CRM integration
* [ ] Real-time company announcements
* [ ] Advanced gesture recognition
* [ ] Multiple company profiles
* [ ] Remote robot configuration
* [ ] Dashboard for monitoring interactions

---

## 📸 Project Demonstration

> Add screenshots, videos, and photographs of the robot interacting with visitors here.

### Robot

<!-- Add project image -->

### Human Detection

<!-- Add project image -->

### AI Chatbot

<!-- Add project image -->

### Robotic Interaction

<!-- Add project image -->

### Exhibition Demonstration

<!-- Add project video/GIF -->

---

## 🔐 Security & Configuration

API keys, credentials, and other sensitive configuration values should **not be committed to the repository**.

Use environment variables or secure configuration mechanisms for:

* AI API keys
* Backend credentials
* Authentication tokens
* Robot application credentials
* External service credentials

---

## 📂 Project Structure

```text
sanBotRobot-HumanInteractive-App/
│
├── android/
│   ├── app/
│   └── ...
│
├── src/
│   ├── components/
│   ├── screens/
│   ├── services/
│   └── ...
│
├── assets/
│   ├── images/
│   └── ...
│
├── README.md
└── ...
```

> The structure above is a general representation. Update it to match the current repository structure.

---

## 📄 Project Purpose

This project demonstrates the practical integration of:

**Artificial Intelligence + Robotics + Android Development + Human Detection + Conversational Interfaces + Human–Robot Interaction**

It is designed as a foundation for developing intelligent robotic assistants for **corporate exhibitions, promotional campaigns, customer engagement, and interactive information services**.

---

## ⭐ Support

If you find this project interesting, consider giving the repository a ⭐ on GitHub.

---

### 🤖 Building the Future of Human–Robot Interaction

> **From static displays to intelligent robotic experiences.**

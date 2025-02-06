# Realize  

**Realize** is an Android utility application that enables users to enhance their captured images with augmented reality (AR). Using **Google ARCore** and **Sceneform**, the app allows users to attach interactive digital content—such as videos, images, links, and PDFs (coming soon)—to real-world photos. This creates a unique way to share memories, experiences, and information using AR technology.

🚨 **Project Status: Abandoned**  
This project has been discontinued due to the deprecation of Sceneform ([SceneView/sceneform-android](https://github.com/SceneView/sceneform-android)) and suboptimal image detection performance, which made it unsuitable for production.

---

## Features  

- 📸 **Capture & Augment** – Click a picture and use it as an AR anchor.  
- 🎥 **Overlay Multimedia** – Attach videos, images, or links on top of real-world objects.  
- 🔗 **Interactive Content Sharing** – Create and share interactive experiences with friends and family.  
- 🌍 **Persistent AR Memories** – Save and revisit past experiences using AR.  
- ☁ **Cloud-Based Storage** – Store and retrieve user-generated AR content via **AWS**.  
- 🔜 **Future Plans (Before Abandonment)**: PDF support, improved AR tracking, and better content discovery.

---

## Tech Stack  

- **Android** (Java)  
- **Google ARCore** – For augmented reality capabilities  
- **Sceneform** – AR rendering (deprecated)  
- **OkHttp** – For API communication  
- **AWS S3** – For cloud-based media storage
- **Glide** – For image manipulation
- **Exoplayer** – For video player
- **Android Youtube Player** – For youtube video embedding
- **Realize Backend** – Handles user data, preferences, and media uploads  

---

## Why Was It Abandoned?  

The project relied on **Sceneform**, which was deprecated, and its image detection capabilities were insufficient for production use. As a result, further development was halted.

---

## Future Possibilities  

Although this implementation has been abandoned, a similar concept could be revived using:  
- **OpenXR or Unity XR** for improved AR tracking.  
- **Vuforia or ARKit/ARCore directly** for better image recognition.  
- **WebAR (like 8thWall)** for a platform-independent experience.  

---

## License  

This project is abandoned but remains open for reference. Feel free to use or adapt parts of it as needed.  

---

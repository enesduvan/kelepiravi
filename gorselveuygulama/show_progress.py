import os
import time
import tkinter as tk
from threading import Thread

FOLDER_PATH = "Duvance_Gorselleri"
TOTAL_EXPECTED = 3000

def update_count():
    while True:
        if os.path.exists(FOLDER_PATH):
            count = len([f for f in os.listdir(FOLDER_PATH) if f.endswith(".jpg")])
        else:
            count = 0
            
        try:
            label_count.config(text=f"İndirilen: {count} / {TOTAL_EXPECTED}")
        except:
            break # Window closed
        time.sleep(1)

root = tk.Tk()
root.title("Duvance Bot Durumu")
root.geometry("320x120")
root.attributes('-topmost', True)

# Center the window
window_width = 320
window_height = 120
screen_width = root.winfo_screenwidth()
screen_height = root.winfo_screenheight()
x_cordinate = int((screen_width/2) - (window_width/2))
y_cordinate = int((screen_height/2) - (window_height/2))
root.geometry(f"{window_width}x{window_height}+{x_cordinate}+{y_cordinate}")

label_title = tk.Label(root, text="Duvance Bot Çalışıyor 🚀", font=("Arial", 14, "bold"), fg="green")
label_title.pack(pady=15)

label_count = tk.Label(root, text="Sayılıyor...", font=("Arial", 16))
label_count.pack()

# Start background thread to monitor folder
t = Thread(target=update_count, daemon=True)
t.start()

root.mainloop()

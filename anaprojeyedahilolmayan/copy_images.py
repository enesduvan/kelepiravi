import os
import shutil
import re

source_dir = r"C:\Users\enes\Documents\antigravity\silly-hertz\test"
target_res_dir = r"c:\Users\enes\AndroidStudioProjects\kelepiravi\app\src\main\res"

folders_to_remove = ["kelepir_avi_512", "kelepir_avi_768", "kelepir_avi_1024"]

for folder in folders_to_remove:
    path = os.path.join(target_res_dir, folder)
    if os.path.exists(path):
        print(f"Removing invalid resource directory: {path}")
        shutil.rmtree(path)

def sanitize_filename(filename):
    name, ext = os.path.splitext(filename)
    
    # Replace dashes with underscores
    name = name.replace('-', '_')
    
    # Convert CamelCase to snake_case
    name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name)
    
    # Lowercase everything
    name = name.lower()
    
    # Remove any characters that are not alphanumeric or underscores
    name = re.sub(r'[^a-z0-9_]', '', name)
    
    # Android resource names must start with a letter
    if not re.match(r'^[a-z]', name):
        name = 'img_' + name
        
    # Replace multiple underscores with a single underscore
    name = re.sub(r'_+', '_', name)
    
    return name + ext

def copy_folder(src_folder_name, target_folder_name):
    src_path = os.path.join(source_dir, src_folder_name)
    target_path = os.path.join(target_res_dir, target_folder_name)
    
    if not os.path.exists(src_path):
        print(f"Source folder not found: {src_path}")
        return

    os.makedirs(target_path, exist_ok=True)
    
    copied = 0
    for filename in os.listdir(src_path):
        if not filename.endswith('.webp') and not filename.endswith('.png') and not filename.endswith('.jpg'):
            continue
            
        src_file = os.path.join(src_path, filename)
        new_filename = sanitize_filename(filename)
        target_file = os.path.join(target_path, new_filename)
        
        shutil.copy2(src_file, target_file)
        copied += 1
        
    print(f"Copied {copied} files from {src_folder_name} to {target_folder_name}")

print("Starting to copy images...")
copy_folder("kelepir_avi_512", "drawable-xxhdpi")
copy_folder("kelepir_avi_1024", "drawable-xxxhdpi")
print("Done!")

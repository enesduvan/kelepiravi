import os
import re

target_res_dir = r"c:\Users\enes\AndroidStudioProjects\kelepiravi\app\src\main\res"

mapping = {
    "_0b175ab2-1456-4a9b-b590-924976292779.webp": "rubber_duck_1",
    "_0da3d6b6-5415-4149-a691-a600eca507da.webp": "tablet_galaxy",
    "_1099b34b-91fa-4aa5-a503-93e128dd8f05.webp": "smartphone_dirty_1",
    "_10ae6615-5087-44eb-9543-ee075514c5b7.webp": "smartphone_clean_1",
    "_1472a365-8e55-47e7-8f9e-1fbcdc1b6c58.webp": "blender_1",
    "_148e8b20-c368-4e1b-bd2a-32859815fdcc.webp": "tablet_scratched_1",
    "_14a872e3-a969-4072-ae69-a47a7ae62ad5.webp": "headphones_duct_tape_1",
    "_15e8d70b-7d8f-469f-9270-680d447f3b1a.webp": "smartphone_scratched_1",
    "_17619f9a-160c-4da0-a424-e6a707388786.webp": "smartphone_cracked_1",
    "_1c2c906e-b2fd-4e7e-9d7a-b53c367da32c.webp": "smartphone_clean_2",
    "_1c61c934-8340-4c26-9cfc-e1f38f6ac11d.webp": "smartphone_clean_3",
    "_1ddec0d6-7502-48fe-9503-3cc2b0a9ebc7.webp": "rubber_duck_2",
    "_27ad0edb-870c-439e-a96e-a42de45fbf79.webp": "canvas_backpack_1",
    "_28e98402-fcf3-4eb8-adb6-a23e913a7198.webp": "dirty_jersey_19",
    "_29008ddc-f528-4746-81e0-aa832ed2e3fd.webp": "smartphone_clean_4",
    "_2c411f82-311e-49ec-8d75-40fac301ed9e.webp": "tablet_scratched_2",
    "_2f88ab4c-f85c-4060-8394-e1e511f57b65.webp": "rubber_duck_3",
    "_3833afdd-a300-4d9d-8a6c-bc5f2bfec60a.webp": "blender_2",
    "_3d739e1e-d504-43dd-a049-e2e5aaabd4a7.webp": "mini_fridge_1",
    "_42e6f3d8-4ba3-41b8-85bf-3afb4324155d.webp": "blender_3",
    "_4398aac0-4ce5-455c-b600-a2e95eec5ded.webp": "smartphone_cracked_2",
    "_46a060d1-9c9b-4836-8d5f-895c770e590f.webp": "classic_watch_1",
    "_4bfb999a-42e4-4798-ba0d-c56173090756.webp": "rubber_duck_4",
    "_509eb3f7-bd2e-4ddc-b460-717002624702.webp": "rubber_duck_5",
    "_541c35fe-e837-4df3-8f14-0f66d4e6ad2f.webp": "laptop_clean_1",
    "_54bc5108-c88e-4087-835c-b78b108c1735.webp": "coffee_maker_1",
    "_59f492e7-61ab-4ce3-bb1a-71490df0d49a.webp": "gamepad_broken_1",
    "_5c49d495-22ff-472d-a105-9df86598bdf3.webp": "smartphone_melted_back",
    "_60694414-9895-430d-869c-354c48e1a31e.webp": "monitor_cracked_1",
    "_740045ec-0195-4728-a7e7-7a43461cbdd2.webp": "classic_bicycle_1",
    "_76869960-0b3a-40c5-b3cc-bb6522408343.webp": "monitor_cracked_2",
    "_76ab9b4b-b256-45c5-acb1-4101576c6d4f.webp": "blender_4",
    "_797151ed-9214-412f-9ed7-f8a058fb9aa3.webp": "headphones_duct_tape_2",
    "_7b2a16ef-bfad-4b13-9ea8-8a4c273f9c95.webp": "mini_fridge_2",
    "_7ef70449-c29f-4c9b-8c5a-da409a175095.webp": "smartphone_cracked_3",
    "_820a6815-c411-4f83-af91-d8ca988ee73d.webp": "smartphone_scratched_2",
    "_8286ddd2-aa2f-4475-8ef9-3ca5b387d5d9.webp": "rubber_duck_6",
    "_84ce2b3a-8f86-41cd-a58b-e7264efb7bbf.webp": "smartphone_clean_5",
    "_8614c883-99d6-4eeb-8dbf-a39333410288.webp": "smartphone_dirty_2",
    "_88885f67-2023-47a9-9d6c-af292fc964fa.webp": "rubber_duck_egg_1",
    "_89225912-0369-4fe3-bcbf-57f9678b3fd1.webp": "smartphone_cracked_4",
    "_8c39c29e-528b-43cd-a717-508cd37f6053.webp": "smartphone_clean_6",
    "_8ea99082-0c1e-4c12-b91a-cfbcd181f6bd.webp": "classic_watch_2",
    "_919130c7-6e2f-4f88-baff-dae3c4919622.webp": "rubber_duck_egg_2",
    "_935b8149-288e-4c74-ac5d-419ecbb12f3c.webp": "smartphone_cracked_5",
    "_987f7b48-1597-45ec-a18d-91b3d2c35ceb.webp": "monitor_cracked_3",
    "_9affb7ba-dde4-4fbc-8f8c-59c7f04221e2.webp": "blender_5",
    "_9d5b2a2e-08e3-4b07-954b-47d94fc24705.webp": "headphones_duct_tape_3",
    "_a63de5a0-0b64-4455-8d46-b7504b1fb6bc.webp": "mini_fridge_3",
    "_aacec2fc-e221-4e96-a83e-50de57a5cc1d.webp": "smartphone_cracked_6",
    "_b555a892-4a06-4c90-a9f7-47c80c88510f.webp": "smartphone_scratched_3",
    "_b79fc217-610f-488e-a684-bb7e4449a246.webp": "rubber_duck_7",
    "_c43a70f0-a09f-4be3-84bf-c5c65349ad56.webp": "smartphone_clean_7",
    "_c80d19b8-3cc2-432d-8b96-0a06b5ec1d99.webp": "smartphone_dirty_3",
    "_cb638fdc-c11b-49bc-bd5c-0b44db6f62e7.webp": "rubber_duck_egg_3",
    "_cc1a1a43-39de-4fb4-9e50-56ab7439e337.webp": "smartphone_cracked_7",
    "_d0035ea6-c340-43ab-9121-0686d46508a3.webp": "smartphone_clean_8",
    "_d2aea9b7-9fc6-4740-a057-1b35bc4946d4.webp": "classic_watch_3",
    "_d68de095-be8f-451e-9e25-bc65af1bfc0b.webp": "rubber_duck_egg_4",
    "_da043745-6556-4490-afdd-887bbdc4692a.webp": "smartphone_cracked_8",
    "_db20fcad-2f13-4996-a65e-9a43764cb5c8.webp": "smartphone_clean_9",
    "_e540fab9-b45d-4878-8e77-3a01f4ba715e.webp": "smartphone_cracked_9",
    "_f5e3660c-8410-4af6-8693-a14f4b8cc385.webp": "smartphone_clean_10",
    "_f67eca19-10b1-41ff-8fed-466ffe9d781c.webp": "canvas_backpack_2",
    "_f77c7acc-b3f0-4fda-834e-49ce8f83d8ee.webp": "smartphone_dirty_4",
    "_f9b72e41-acdb-45ce-ad14-fe0c32fd7513.webp": "rubber_duck_egg_5",
    "_fdc5680b-e81f-4aa0-9dd3-a85eda121199.webp": "smartphone_cracked_10",
    "_ffd33a52-5a1e-4920-85ad-f0dec93e3d0b.webp": "coffee_maker_2"
}

def sanitize_filename(filename):
    name, ext = os.path.splitext(filename)
    name = name.replace('-', '_')
    name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name)
    name = name.lower()
    name = re.sub(r'[^a-z0-9_]', '', name)
    if not re.match(r'^[a-z]', name):
        name = 'img_' + name
    name = re.sub(r'_+', '_', name)
    return name + ext

renamed_count = 0
for folder in ["drawable-xxhdpi", "drawable-xxxhdpi"]:
    folder_path = os.path.join(target_res_dir, folder)
    if not os.path.exists(folder_path):
        continue
    
    for orig_name, new_name in mapping.items():
        current_name = sanitize_filename(orig_name)
        current_path = os.path.join(folder_path, current_name)
        
        _, ext = os.path.splitext(orig_name)
        new_filename = new_name + ext
        new_path = os.path.join(folder_path, new_filename)
        
        if os.path.exists(current_path):
            os.rename(current_path, new_path)
            renamed_count += 1

print(f"Successfully renamed {renamed_count} files!")

import zipfile
import xml.etree.ElementTree as ET

def get_docx_text(path):
    doc = zipfile.ZipFile(path)
    xml_content = doc.read('word/document.xml')
    doc.close()
    tree = ET.XML(xml_content)
    NAMESPACE = '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
    paragraphs = []
    for paragraph in tree.iter(NAMESPACE + 'p'):
        texts = [node.text for node in paragraph.iter(NAMESPACE + 't') if node.text]
        if texts:
            paragraphs.append(''.join(texts))
    return '\n'.join(paragraphs)

text = get_docx_text('event.docx')
with open('event.txt', 'w', encoding='utf-8') as f:
    f.write(text)

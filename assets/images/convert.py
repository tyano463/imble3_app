from PIL import Image
import sys
import os

argv = sys.argv

def remove_ext(fpath):
    d = os.path.dirname(fpath)
    fname = os.path.basename(fpath)
    ff, ext = os.path.splitext(fname)
    prefix = os.path.join(d, ff)
    return prefix
    

def resize_image(input_path):
    # 変換するサイズ
    sizes = [48,72,96,108,144,162,192,216,324,432]


    prefix = remove_ext(input_path)
    
    # 入力画像を開く
    with Image.open(input_path) as img:
        # 幅と高さが同じであることを確認
        if img.width != img.height:
            raise ValueError("Input image must have equal width and height.")
        
        # サイズごとに画像をリサイズして保存
        for size in sizes:
            resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
            output_path = f"{prefix}_{size}x{size}.png"
            resized_img.save(output_path, format='PNG')
            print(f"Saved resized image to {output_path}")

def usage():
    global argv

    s = f"""
usage:
    {argv[0]} [input image]
"""
    print(s)
    
def arg_check():
    global argv    

    if len(argv) < 2:
        usage()
        exit()

# 使用例
#input_image_path = "input_image.png"  # 入力画像のパス
#output_directory = "output_images"    # 出力ディレクトリ

arg_check()
fpath = argv[1]
#print(prefix)

resize_image(argv[1])



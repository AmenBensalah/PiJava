import os
import re

directory = r'c:\Users\ilyes\Downloads\Workshop-JDBC-JavaFX\src\main\java\edu\projetJava\controllers'

def fix_fullscreen():
    for filename in os.listdir(directory):
        if not filename.endswith('.java'):
            continue
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        modified = False
        new_lines = []
        i = 0
        while i < len(lines):
            line = lines[i]
            new_lines.append(line)
            
            # Match stage.setScene(...) but not alertStage.setScene(...) to avoid making alerts fullscreen
            match = re.search(r'^(\s+)(stage|window)\.setScene\(', line)
            if match:
                indent = match.group(1)
                stage_var = match.group(2)
                # check next line
                next_line = lines[i+1] if i+1 < len(lines) else ""
                if "setFullScreen" not in next_line:
                    new_lines.append(f"{indent}{stage_var}.setFullScreen(true);\n")
                    modified = True
            i += 1
            
        if modified:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            print(f"Updated {filename}")

fix_fullscreen()

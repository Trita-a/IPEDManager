"""
IPEDManager Build Script
Author: William Tritapepe
"""
import zipfile
import os
import shutil
import subprocess
import sys

# Paths
IPED_JRE = r"c:\Users\Forensic_Dell\Desktop\nuova imo\Iped\IPED-releasee\iped-4.2.2\jre\bin\java.exe"
ECJ_JAR = "tools/ecj_correct.jar"
STUB_SIZE = 106496

def compile_sources():
    """Compila i sorgenti Java con ECJ."""
    print("[1/4] Compilazione sorgenti Java...")
    
    # Pulisci output
    if os.path.exists("out"):
        shutil.rmtree("out")
    os.makedirs("out")
    
    # Trova tutti i file .java ricorsivamente
    sources = []
    for root, dirs, files in os.walk("src"):
        for f in files:
            if f.endswith(".java"):
                sources.append(os.path.join(root, f))
    
    if not sources:
        print("ERRORE: Nessun file sorgente trovato!")
        return False
    
    print(f"  Trovati {len(sources)} file sorgente...")
    
    cmd = [
        IPED_JRE, "-jar", ECJ_JAR,
        "-classpath", "lib/flatlaf-3.2.5.jar",
        "-source", "1.8", "-target", "1.8",
        "-d", "out", "-encoding", "UTF-8"
    ] + sources
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    if "error" in result.stderr.lower():
        print(f"ERRORE: {result.stderr}")
        with open("compile_error.txt", "w", encoding="utf-8") as f:
            f.write(result.stderr)
        return False
    print("  Compilazione completata.")
    return True

def copy_resources():
    """Copia le risorse nella cartella output."""
    print("[2/4] Copia risorse...")
    
    # 1. Copia risorse globali (icons, ecc.)
    if os.path.exists("resources"):
        shutil.copytree("resources", "out", dirs_exist_ok=True)
    
    # 2. Copia risorse dai sorgenti (file .properties, ecc.) mantenendo la struttura
    count = 0
    for root, dirs, files in os.walk("src"):
        for file in files:
            if not file.endswith(".java"):
                src_path = os.path.join(root, file)
                # Calcola percorso relativo rispetto a src
                rel_path = os.path.relpath(src_path, "src")
                dest_path = os.path.join("out", rel_path)
                
                os.makedirs(os.path.dirname(dest_path), exist_ok=True)
                shutil.copy2(src_path, dest_path)
                count += 1
                
    print(f"  Risorse copiate (Globali + {count} da src).")
    
    # Crea META-INF/MANIFEST.MF
    os.makedirs("out/META-INF", exist_ok=True)
    shutil.copy("MANIFEST.MF", "out/META-INF/MANIFEST.MF")

def create_jar():
    """Crea il JAR combinato con FlatLaf."""
    print("[3/4] Creazione JAR...")
    
    combined_jar = "IPEDManager_combined.jar"
    
    # Estrai FlatLaf in temp
    temp_dir = "temp_combined"
    if os.path.exists(temp_dir):
        shutil.rmtree(temp_dir)
    
    with zipfile.ZipFile("lib/flatlaf-3.2.5.jar", 'r') as zf:
        zf.extractall(temp_dir)
    
    # Copia output compilato (sovrascrive)
    shutil.copytree("out", temp_dir, dirs_exist_ok=True)
    
    # Crea JAR
    if os.path.exists(combined_jar):
        os.remove(combined_jar)
    
    with zipfile.ZipFile(combined_jar, 'w', zipfile.ZIP_DEFLATED) as zf:
        for root, dirs, files in os.walk(temp_dir):
            for f in files:
                file_path = os.path.join(root, f)
                arc_name = os.path.relpath(file_path, temp_dir)
                zf.write(file_path, arc_name)
    
    shutil.rmtree(temp_dir)
    print(f"  JAR creato: {os.path.getsize(combined_jar) / 1024:.1f} KB")
    return combined_jar

def create_exe(jar_path):
    """Crea l'EXE usando Launch4j con i metadati corretti."""
    print("[4/4] Creazione EXE con Launch4j...")
    
    launch4j_exe = r"C:\Program Files (x86)\Launch4j\launch4jc.exe"
    config_file = "launch4j.xml"
    output_exe = "IPEDManager.exe"
    
    if not os.path.exists(launch4j_exe):
        print(f"ERRORE: Launch4j non trovato in: {launch4j_exe}")
        return None
    
    # Esegui Launch4j
    cmd = [launch4j_exe, config_file]
    result = subprocess.run(cmd, capture_output=True, text=True)
    
    if result.returncode != 0 or not os.path.exists(output_exe):
        print(f"ERRORE Launch4j (Exit Code {result.returncode}):")
        print(f"STDERR: {result.stderr}")
        print(f"STDOUT: {result.stdout}")
        return None
    
    size_mb = os.path.getsize(output_exe) / 1024 / 1024
    print(f"  EXE creato: {output_exe} ({size_mb:.2f} MB)")
    return output_exe

def deploy_exe(exe_path):
    """Copia l'EXE generato nella cartella di release."""
    print("[5/5] Distribuzione...")
    
    deploy_dir = r"c:\Users\Forensic_Dell\Desktop\nuova imo\Iped\IPED-releasee\iped-4.2.2"
    target_path = os.path.join(deploy_dir, "IPEDManager.exe")
    
    if not os.path.exists(deploy_dir):
        print(f"ATTENZIONE: Cartella di destinazione non trovata: {deploy_dir}")
        return
        
    try:
        shutil.copy2(exe_path, target_path)
        print(f"  Copiato in: {target_path}")
        
        # Copia anche lo script wrapper .bat
        bat_file = "IPEDManager.bat"
        if os.path.exists(bat_file):
            shutil.copy2(bat_file, os.path.join(deploy_dir, bat_file))
            print(f"  Copiato script wrapper: {bat_file}")
    except Exception as e:
        print(f"ERRORE durante la copia: {e}")

def deploy_defaults():
    """Copia la cartella defaults nella cartella di release."""
    source_defaults = "defaults"
    deploy_dir = r"c:\Users\Forensic_Dell\Desktop\nuova imo\Iped\IPED-releasee\iped-4.2.2"
    target_defaults = os.path.join(deploy_dir, "defaults")

    if not os.path.exists(source_defaults):
        print("ATTENZIONE: Cartella defaults sorgente non trovata.")
        return

    print(f"  Aggiornamento defaults in: {target_defaults}")
    if os.path.exists(target_defaults):
        shutil.rmtree(target_defaults)
    shutil.copytree(source_defaults, target_defaults)

def main():
    print("=" * 50)
    print("  IPEDManager Build Script")
    print("  Author: William Tritapepe")
    print("=" * 50)
    print()
    
    if not compile_sources():
        sys.exit(1)
    
    copy_resources()
    jar = create_jar()
    exe = create_exe(jar)
    deploy_exe(exe)
    deploy_defaults()
    
    print()
    print("=" * 50)
    print("  BUILD E DISTRIBUZIONE COMPLETATI!")
    print(f"  File locale: {exe}")
    print("=" * 50)

if __name__ == "__main__":
    main()

import os
import requests
import argparse

OSM_URL = "https://download.geofabrik.de"
CHUNK_SIZE = 16 * 1024

def download_procedure(url, path, timeout):
    tmp_path = path + ".part"  # file temporaneo
    try:
        with requests.get(url, stream=True, timeout=timeout) as r:
            r.raise_for_status()
            total_length = r.headers.get('content-length')
            
            if total_length is None:
                with open(tmp_path, 'wb') as f:
                    f.write(r.content)
            else:
                total_length = int(total_length)
                downloaded = 0
                last_logged_percent = 0
                with open(tmp_path, 'wb') as f:
                    for chunk in r.iter_content(chunk_size=CHUNK_SIZE):
                        if chunk:
                            f.write(chunk)
                            downloaded += len(chunk)
                            percent = int(downloaded / total_length * 100)
                            if percent // 10 > last_logged_percent // 10:
                                last_logged_percent = percent
                                print(f"Download progress: {percent}%")
        os.rename(tmp_path, path)
        print("Download complete!")
    except KeyboardInterrupt:
        print("\nDownload interrotto manualmente.")
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
    except Exception as e:
        print("\nDownload fallito.")
        if os.path.exists(tmp_path):
            os.remove(tmp_path)
        raise

def download_map(continent, country, directory, filename, timeout):
    if not filename:
        filename = f"{country}-latest.osm.pbf"
    url = f"{OSM_URL}/{continent}/{country}-latest.osm.pbf"
    directory = os.path.abspath(os.path.join("..", directory))
    path = os.path.join(directory, filename)

    os.makedirs(directory, exist_ok=True)

    if os.path.exists(path):
        print(f"Map already exists at: {path}")
        return

    print(f"Downloading {url} to {path} ...")
    
    download_procedure(url,path,timeout)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Download OSM map manually")
    parser.add_argument("--continent", default="europe")
    parser.add_argument("--country", default="italy")
    parser.add_argument("--directory", default="data")
    parser.add_argument("--filename", default=None)
    parser.add_argument("--timeout", type=int, default=120, help="Timeout in seconds")

    args = parser.parse_args()
    download_map(args.continent, args.country, args.directory, args.filename, args.timeout)

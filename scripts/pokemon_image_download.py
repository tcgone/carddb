# import urllib.request


# baseURL = "https://images.pokemontcg.io/sm11/"
# URL_end = "_hires.png"
# for x in range(1, 258):
#     fullURL = baseURL + str(x) + URL_end
#     filename = str(x) + ".png"
#     print("URL: " + fullURL)
#     urllib.request.urlretrieve(fullURL, filename)
#     print("Downloaded " + filename)


import requests
import shutil

set_name = "sma/"
set_prefix = "SV"
baseURL = "https://images.pokemontcg.io/" + set_name + set_prefix
URL_end = "_hires.png"

for x in range(1, 95):
    digitFiller = ""
    if x < 100:
        digitFiller = "0"
    if x < 10:
        digitFiller = "00"

    fullURL = baseURL + str(x) + URL_end
    filename = digitFiller + str(x) + ".png"
    print("URL: " + fullURL)
    
    r = requests.get(fullURL, stream=True)
    if r.status_code == 200:
        with open(filename, 'wb') as f:
            r.raw.decode_content = True
            shutil.copyfileobj(r.raw, f)

    print("Downloaded " + filename)
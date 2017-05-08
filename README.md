# CS504-Homework3


Change Crawler to make it support

1.1 clean and tokenize title and query (refer to dedupe_ads.py)

1.2 set Ads keywords with tokenized title

1.3 paging

Generate sub query and crawl data with sub query

2.1 for each raw query in rawQuery3.txt, if length of the query >= 3, generate n-gram 2<=n <= len(query) -1 , programming language is not required

2.2 for each sub query generated , if it's not in current rawQuery3.txt, send it to Crawler and crawl corresponding data. we only need the ads with same category as raw query's category.

Usage: 
    Crawler \<rawQueryDataFilePath\> \<adsDataFilePath\> \<proxyFilePath\> \<logFilePath\> \<subQueryFilePath\>

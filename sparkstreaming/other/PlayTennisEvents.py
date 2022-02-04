from csv import reader
import json
from functools import reduce
import heapq
import time
import os

def convertMatchTimeToInt(matchTime: str) -> int:
    def cvt(tuple1: int ) -> int:
        return 60**tuple1[0]*tuple1[1]

    ints = list(map(int, matchTime.split(":")))
    mapped = list(map(cvt, enumerate(reversed(ints))))
    reduced = reduce(lambda x,y: x+y ,mapped)
    return reduced

def sourceFile()-> str:
    if os.name == "posix":
        return "/mnt/c/MyWork/GIT/scala/TestMe/resource/keystrokes-for-tech-test.csv"
    else:
        return r"C:\MyWork\GIT\scala\TestMe\resource\keystrokes-for-tech-test.csv"


def play(file: str):

    heap = []
    heapq.heapify(heap)

    with open(file) as csvfile:
        f = reader(csvfile)
        next(f) # skip header
        for row in f:
            #print(row)
            match_id = row[1]
            json_str = row[3]
            json_str = json_str.replace('"','').replace("'",'"')
            json_obj = json.loads(json_str)
            heapq.heappush(heap, (convertMatchTimeToInt(json_obj["matchTime"]), ",".join(row)))


    lastSleep = 0
    while heap:
        item = heapq.heappop(heap)
        print(item[1])
        sleepFor = item[0] - lastSleep
        lastSleep = item[0]
        time.sleep(sleepFor)

if __name__ == '__main__':
    file = sourceFile()
    play(file)
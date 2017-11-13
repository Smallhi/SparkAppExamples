from bs4 import BeautifulSoup
from selenium import webdriver
import time
import sys
import io
# sys.setdefaultencoding('utf8')
# TODO 如何刷赞
# TODO 微信公众号如何阅读量和赞
# 解决控制台输出和Scala 不兼容问题
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf8')
url = 'http://www.jianshu.com/p/c20351c842bc'
# driver = webdriver.PhantomJS()
# 在pycharm 中必须指明PhantomJS的绝对路径
driver = webdriver.PhantomJS("/Users/hhl/PycharmProjects/phantomjs-2.1.1-macosx/bin/phantomjs")

for i in range(2):
    time.sleep(2)
    driver.get(url)
    soup = BeautifulSoup(driver.page_source, 'xml')
    titles = soup.find_all('h1', {'class': 'title'})
    nums = soup.find_all('span', {'class': 'views-count'})
    for title, num in zip(titles, nums):
        print(title.get_text(), num.get_text())



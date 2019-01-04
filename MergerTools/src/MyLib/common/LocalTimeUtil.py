# -*- coding: gbk -*-
'''
Created on 2017��3��28��
@author: xmnathan
'''

from datetime import datetime
import time


#ʱ�任�㶨��
s2ms = 1000  #��->����
ms2s = 0.001 #����->��
m2s=60       #����->��


def timestamp_to_strtime(timestamp):
    """�� 13 λ�����ĺ���ʱ���ת���ɱ�����ͨʱ�� (�ַ�����ʽ)
    :param timestamp: 13 λ�����ĺ���ʱ��� (1456402864242)
    :return: �����ַ�����ʽ {str}'2016-02-25 20:21:04.242000'
    """
    local_str_time = datetime.fromtimestamp(timestamp / 1000.0).strftime('%Y-%m-%d %H:%M:%S.%f')
    return local_str_time


def timestamp_to_datetime(timestamp):
    """�� 13 λ�����ĺ���ʱ���ת���ɱ�����ͨʱ�� (datetime ��ʽ)
    :param timestamp: 13 λ�����ĺ���ʱ��� (1456402864242)
    :return: ���� datetime ��ʽ {datetime}2016-02-25 20:21:04.242000
    """
    local_dt_time = datetime.fromtimestamp(timestamp / 1000.0)
    return local_dt_time


def datetime_to_strtime(datetime_obj):
    """�� datetime ��ʽ��ʱ�� (������) תΪ�ַ�����ʽ
    :param datetime_obj: {datetime}2016-02-25 20:21:04.242000
    :return: {str}'2016-02-25 20:21:04.242'
    """
    local_str_time = datetime_obj.strftime("%Y-%m-%d %H:%M:%S.%f")
    return local_str_time


def datetime_to_timestamp(datetime_obj):
    """������(local) datetime ��ʽ��ʱ�� (������) תΪ����ʱ���
    :param datetime_obj: {datetime}2016-02-25 20:21:04.242000
    :return: 13 λ�ĺ���ʱ���  1456402864242
    """
    local_timestamp = long(time.mktime(datetime_obj.timetuple()) * 1000.0 + datetime_obj.microsecond / 1000.0)
    return local_timestamp

def strtime_to_datetime(timestr):
    """���ַ�����ʽ��ʱ�� (������) תΪ datetiem ��ʽ
    :param timestr: {str}'2016-02-25 20:21:04.242'
    :return: {datetime}2016-02-25 20:21:04.242000
    """
    local_datetime = datetime.strptime(timestr, "%Y-%m-%d %H:%M:%S.%f")
    return local_datetime

def strtime_to_timestamp(local_timestr):
    """������ʱ�� (�ַ�����ʽ��������) תΪ 13 λ�����ĺ���ʱ���
    :param local_timestr: {str}'2016-02-25 20:21:04.242'
    :return: 1456402864242
    """
    local_datetime = strtime_to_datetime(local_timestr)
    timestamp = datetime_to_timestamp(local_datetime)
    return timestamp

def current_datetime():
    """���ر��ص�ǰʱ��, ����datetime ��ʽ, �ַ�����ʽ, ʱ�����ʽ
    :return: (datetime ��ʽ, �ַ�����ʽ, ʱ�����ʽ)
    """
    # ��ǰʱ�䣺datetime ��ʽ
    local_datetime_now = datetime.now()
    # ��ǰʱ�䣺�ַ�����ʽ
    local_strtime_now = datetime_to_strtime(local_datetime_now)
    # ��ǰʱ�䣺ʱ�����ʽ 13λ����
    local_timestamp_now = datetime_to_timestamp(local_datetime_now)
    return local_datetime_now, local_strtime_now, local_timestamp_now

#�����ַ���ʱ��
def current_strtime():
    return datetime_to_strtime(datetime.now())
    
#���ص�ǰʱ���
def curTimeStamp():
    return datetime_to_timestamp(datetime.now())

#������x��ͣ
def sleep_ms(milliseconds):
    time.sleep(milliseconds*ms2s)
    
#test
if __name__ == "__main__":
    time_str = '2017-03-28 16:21:04.323'
    timestamp1 = strtime_to_timestamp(time_str)
    datetime1 = strtime_to_datetime(time_str)
    time_str2 = datetime_to_strtime(datetime1)
    timestamp2 = datetime_to_timestamp(datetime1)
    datetime3 = timestamp_to_datetime(timestamp2)
    time_str3 = timestamp_to_strtime(timestamp2)
    current_time = current_datetime()
    
    print 'timestamp1: ', timestamp1
    print 'datetime1: ', datetime1
    print 'time_str2: ', time_str2
    print 'timestamp2: ', timestamp2
    print 'datetime3: ', datetime3
    print 'time_str3: ', time_str3
    print 'current_time: ', current_time
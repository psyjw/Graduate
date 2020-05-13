require 'dm-core'
require 'dm-migrations'
require 'dm-serializer'
require 'dm-types'

class Gambler
    include DataMapper::Resource
    property :id, Serial
    property :usrname, String
    property :password, String
    property :win, Integer
    property :loss, Integer
    property :profit, Integer
end

DataMapper.finalize
DataMapper.setup(:default, "sqlite3://#{Dir.pwd}/gambler.db")
#DataMapper.auto_upgrader!
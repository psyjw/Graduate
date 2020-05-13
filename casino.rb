require 'sinatra'
require 'sinatra/reloader'
require './database'

configure do
    enable :sessions
end

get '/' do
    if session[:login] == true
        erb :main
    else
        erb :login
    end
end

get '/login' do
    if session[:login] == true
        erb :main
    else
        erb :login
    end
end

post '/login' do
    user = Gambler.first(usrname: params[:username])
    if (user==nil)
        session[:message] = "User does not exist!"
        erb :login
    elsif user.password != params[:password]
        session[:message] = "Username and password does not match!"
        erb :login
    else
        session[:login] = true
        session[:user] = user
        session[:totalwin] = 0
        session[:totalloss] = 0
        session[:totalprofit] = 0      
        redirect './main'
    end 
end

get '/logout' do
    session.clear
    session[:message] = "You have sucessfully logged out."
    redirect './login'
end

get '/main' do
    if session[:login] != true
        redirect './login'
    end
    @curuser = session[:user]
    @sessionwin = session[:totalwin]
    @sessionloss = session[:totalloss]
    @sessionprofit = session[:totalprofit]
    erb :main
end

post '/bet' do
    betting(params[:betMoney], params[:betNumber])
    redirect './main'
end

def betting(stake, number)
    if stake == nil or number == nil
        session[:message] = "Invalid Bet!"
        return
    end

    stake = stake.to_i
    number = number.to_i

    res = rand(6)+1
    win = session[:user].win
    loss = session[:user].loss
    profit = session[:user].profit
    
    if res == number
        session[:totalwin] += stake
        session[:totalprofit] += stake
        session[:user].update(win: win+stake)
        session[:user].update(profit: profit+stake)
    else
        session[:totalloss] += stake
        session[:totalprofit] -=stake
        session[:user].update(loss: loss+stake)
        session[:user].update(profit: profit-stake)
    end
end


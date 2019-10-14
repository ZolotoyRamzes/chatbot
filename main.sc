require: text/text.sc
    module = zenbot-common
require: number/number.sc
    module = zenbot-common
require: city/city.sc
    var = $City
    module = zenbot-common
    name = City
require: city/cities-ru.csv
    var = $Cities
    module = zenbot-common
    name = Cities
require: common.js
    module = zenbot-common
require: functions.js
require: patterns.sc


require: newSessionOnStart/newSession.sc
    module = zenbot-common
    injector = {
        newSessionStartState: "/NewSessionWelcome"
        }

theme: /

    init:
        $global.startState = $injector.newSessionStartState || "/";
        $global.newSession = function($context) {
            var query = $context.request.query || "/start";
            $context.request.data.newSession = true;
            $context.request.data.targetState = $context.temp.targetState || $context.currentState;
            $reactions.newSession({message: query, data: $context.request.data});
        }
   
    
    state: NewSessionWelcome
       q: Новая сессия || toState = /Start
       go!: /Beginning
   
    state: Start
        q!: * *start
        script:
            if (!$request.data.newSession) {
                newSession($context);
            }
        go!: {{ startState }}
   
    state: Beginning
        q!: $Beginning
        a: Привет, в какую игру будем играть? Числа или Города?
        go!:/SelectGame
    
    state: SelectGame
    
        state: Numbers
            q: $Numbers
            a: Кто начинает? Пользователь или Бот
            go: /SelectUserOrBot

        state: Cities
            q: $CitiesGame
            a: Кто начинает? Пользователь или  Бот
            go: /CitiesSelectUserOrBot

    
    state: CitiesSelectUserOrBot
    
        state: StartCitiesUser
            q: {[ок*] [ну] [давай*] [начну] [начинаю] (Пользователь|человек|я) }
            a: Назовите город и мы начнем.
            script: $session.processedIds = [];
                //Инициализируем переменную
                $session.lastCityLetter = '';
            go: /CitiesGuessStartUser

            state: Changechoice
                q!: { [давай] [лучше] [все*] [(ты |бот| компьютер)] }
                a: Хорошо. Давай поменяемся. 
                go!: /StartCitiesBot

        state: StartCitiesBot
            q: { [ок*][ну][давай*] [начни] [начина*] (комп*|бот*|ты|машина) }
            a: Хорошо. Начнем: Москва
            script: $session.processedIds = [];
                //Если назвали город первыми, записываем последний символ города
                $session.lastCityLetter = 'а'; //Русская а
            go: /CitiesGuessStartUser
            

    state: SelectUserOrBot

        
        state: StartNumbersUser
            q: [ок*] { [ну] [давай*] [начну] [начинаю] (Пользователь|человек|я) }
            a: Загадайте число от 1 до 100.
            go: /NumberGame/GuessStart
        
            state: Changechoice
                q!: { [лучше] [все*] [(ты |бот| компьютер)] }
                a: Хорошо. Давай поменяемся. 
                go!:/StartNumbersBot    
                
        state: StartNumbersBot
            q: {[ок*] [ну] [давай*] [начни] [начина*] (комп*|бот*|ты|машина) }
            a: Загадал. Жду варианты.
            script:
                var randomNumber = Math.floor(Math.random() * 100) +1;
                $session.randomNumber = randomNumber;
            go: /NumberGame/GuessStartBot
            
            state: Changechoice
                q!: {[давай][лучше] [все*] [(я |человек| пользователь)] }
                a: Хорошо. Давай поменяемся. 
                go!: /SelectUserOrBot/StartNumbersUser

    state: NumberGame

        state: Cityout
            q: $City
            a: Так вы хотите поиграть в города?
        
            state: Yes
               q!: $Yes
               a: Отлично! Выберите кто загадывает.
               go:/CitiesSelectUserOrBot
            
            state: No
               q!:$No
               a: Ну вот! :( Тогда до свидания! 

        state: GuessStart
            q!:$GuessStart
            a: 50
            script:
                $session.startInterval = 0;
                $session.endInterval = 100;
                $session.lastNumber = 50;
            go!: /NumberGame/Guess

        state: Guess
            a: Загаданное число больше или меньше указанного? 
        
            state: More
                q: $More
                script:
                    $session.startInterval = $session.lastNumber;
                    var lastNum = $session.lastNumber;
                    $session.lastNumber = $session.lastNumber + Math.floor( ($session.endInterval - $session.startInterval)/2);
                    if (lastNum == $session.lastNumber) {
                    $session.startInterval = $session.endInterval;
                    $session.endInterval = 100;
                    $session.lastNumber = $session.lastNumber + Math.floor( ($session.endInterval - $session.startInterval)/2);
                    }
                    $reactions.answer($session.lastNumber);
                go: /NumberGame/Guess

            state: Less
                q: $Less
                script:
                    $session.endInterval = $session.lastNumber;
                    var lastNum = $session.lastNumber;
                    $session.lastNumber = Math.abs($session.lastNumber - Math.floor( ($session.endInterval - $session.startInterval)/2));;
                    if (lastNum == $session.lastNumber) {
                    $session.startInterval = $session.endInterval;  
                    $session.startInterval = 0;
                    $session.lastNumber = Math.abs($session.lastNumber - Math.floor( ($session.endInterval - $session.startInterval)/2));;
                    }
                    $reactions.answer($session.lastNumber);
                go: /NumberGame/Guess
        
            state: RightGuess
                q: $RightGuess
                a: Я - молодец! 
                go: /Next
       
            state: ChangeGameCities
                q!: $ChangeGameCities 
                a: Окей.Сменим игру. Кто начнет?
                go!: /CitiesSelectUserOrBot
     
        state: GuessStartBot
            q!: $NumberDigit 
            script:
               var number = $session.number;
               //$reactions.answer($session.randomNumber);
               if (+$session.randomNumber > +$parseTree.text) {
               $reactions.answer('Больше!'); 
               } else if (+$session.randomNumber < $parseTree.text) {
               $reactions.answer('Меньше!'); 
               } else {
               $reactions.answer('Верно!Ты победил!');
               }
            go: /Next 
            
            
            state: Changechoice
                q!: {[давай][лучше] [все*] [(я |человек| пользователь)] }
                a: Хорошо. Давай поменяемся. 
                go!: /SelectUserOrBot/StartNumbersUser
                
            
    state: Emotions
        q: (Ура! еее)
        go: /Next
    
    state: Next
        a: Ну что продолжим играть?
        
        state: Yes
            q!: $Yes
            a: Отлично! Во что сыграем?
            go:/SelectGame
        
        state: No
            q!:$No
            a: Ну вот! :( Тогда до встречи! Буду ждать!
            
    state: YouLose
        q: $YouLose
        a: Ты проиграл!
        go: /Stop
        
 
    state: CityGame
         
        state: CitiesGuessStartUser
           q!: $City
           script:
            var word = $parseTree.text;
            if ($session.lastCityLetter == '' || word[0].toLowerCase() == $session.lastCityLetter.toLowerCase()) {
            var lastLetter = word[word.length - 1];
            $session.botsLastCityLetter = lastLetter;
            if($parseTree.text[$parseTree.text.length-1] !== $session.botsLastCityLetter) { 
            $reactions.answer('Не пойдет!'); }
            var city = findCity($parseTree, $Cities, $session.processedIds);
            if (city) { //проверка на случай null
                $session.processedIds.push(city.id);
                $session.lastCityLetter = city.value.name[city.value.name.length - 1];
                $reactions.answer(city.value.name);
            } else {
                $reactions.answer('Я сдаюсь, ты победил!');
            }
            } else {
            $reactions.answer('Вы пытаетесь меня обмануть! Назовите город на букву "'
            + $session.lastCityLetter + '"');
            }
        
        state: noMatch
            q: $oneWord
            a: Я вас не понял!  
            
        state: Exceptions
            q: (СПб|Мск|Ленинград|Петроград|Питер)
            a: Не подходит! 
        
        state: ChangeGameNumbers
           q!: $ChangeGameNumbers 
           a: Окей.Сменим игру. Кто начнет? 
           go!: /SelectUserOrBot
        
        state: Numberout
            q: $NumberDigit
            a: Так вы хотите поиграть в числа?
          
            state: Yes
               q!: $Yes
               a: Отлично! Выберите кто загадывает.
               go:/SelectUserOrBot
        
            state: No
                q!:$No
                a: Ну вот! :( Тогда до свидания!
    
    
    state: Cityout
        q: $City
        a: Так вы хотите поиграть в города?
        
        state: Yes
            q!: $Yes
            a: Отлично! Выберите кто загадывает.
            go:/CitiesSelectUserOrBot
            
        state: No
            q!:$No
            a: Ну вот! :( Тогда до свидания!          
    
                 
    state: endGame
        q: Хорош
        a: Ок
        go:/Next

        state: Next
        a: Ну что продолжим играть?
        
            state: Yes
               q!: $Yes
               a: Отлично! Во что сыграем?
               go:/SelectGame
        
            state: No
               q!:$No
                a: Ну вот! :( Тогда до встречи! Буду ждать!
        
    state: Stop
        q!: $Stop
        a: Окей. Можем начать сначала.В какую игру еще сыграем? Числа или Города?
        go!: /SelectGame
        
    state: Talk
        q!: $Talk
        a: Не отходите от темы! Я - игровой бот и должен играть с вами. 
        go: /Next
    
    state: Goodbye
        q!: $Goodbye
        a: Уже уходите? Может сыграем в игру?
    
        state: Yes
            q!: $Yes
            a: Отлично!
            go:/SelectGame
        
        state: No
            q!:$No
            a: Ну вот! :( Тогда до встречи! Буду ждать!
         
    state: Lazy
        q!: $Lazy
        a: Не ленитесь!
        go: /Next
           
        state: Next
        a: Ну что продолжим играть?
        
            state: Yes
                q!: $Yes
                a: Отлично!
                go:/SelectGame
        
            state: No
                q!:$No
                a: Ну вот! :( Тогда до встречи! Буду ждать!
        
    state: Nobody
        q!: (никто)
        a: Так не пойдет!
        go: /Next
            
        state: Next
        a: Ну что продолжим играть?
        
            state: Yes
                q!: $Yes
                a: Отлично! Выберите игру!
                go:/SelectGame
        
            state: No
                q!:$No
                a: Ну вот! :( Тогда до встречи! Буду ждать!
    
    state: NoMatch
        q: *
        a: Я вас не понял!
        go: /Next



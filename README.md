# registration-service
Registration service module

![Java](https://img.shields.io/badge/-Java-05122A?style=flat&logo=Java&logoColor=fffffb) ![Spring](https://img.shields.io/badge/-Spring-05122A?style=flat&logo=Spring) ![Springboot](https://img.shields.io/badge/-Springboot-05122A?style=flat&logo=Springboot) ![H2](https://img.shields.io/badge/-H2-05122A?style=flat&logo=h2) ![JUnit](https://img.shields.io/badge/-JUnit-05122A?style=flat&logo=JUnit) ![Mockito](https://img.shields.io/badge/-Mockito-05122A?style=flat&logo=Mockito) ![Powermock](https://img.shields.io/badge/-Powermock-05122A?style=flat&logo=Powermock) ![Liquibase](https://img.shields.io/badge/-Liquibase-05122A?style=flat&logo=Liquibase) ![Lombok](https://img.shields.io/badge/-Lombok-05122A?style=flat&logo=Lombok) ![Rest](https://img.shields.io/badge/-RestAPI-05122A?style=flat&logo=rest) ![Postman](https://img.shields.io/badge/-Postman-05122A?style=flat&logo=Postman)

## Main Features:
* Java 11,
* SpringBoot (web, data-jpa),
* H2,
* JUnit, Mockito, PowerMock, AssertJ (Юнит тесты 79% строк кода).
* Для удобства ручного тестирования в папке data приложена коллекция с запросами для Postman,
* Liquibase (создание таблиц и наполнение некоторыми тестовыми данными),
* Lombok

### Выполненное тестовое задание SKB LAB.
Алгоритм работы:
* Контроллер получает данные из формы регистрации **POST** запросом на **/api/auth/** в формате **JSON** 
со следующими обязательными полями: email, login, password, firstname, lastname, middlename (опционально).
* Передает данные из запроса в RegistrationService, где сначала происходит проверка введенных данных 
заполнение, формат Email, соответствие пароля, отсутствие посторонних символов в имени/фамилии/отчестве. 
В случае каких-либо ошибок, собирает все ошибки в List<String> и возвращает в контроллер.
* После первичной проверки, пытается отправить данные на одобрение регистрации во внешний сервис 
используя MessagingService, которая получает данные и передает их во внешнюю службу на проверку, 
а также возвращает Id в RegistrationService, по которому можно узнать ответ.
* После получения ответа от внешней службы MessageService сохраняет результат в Map<'id', boolean>.
* Далее RegistrationService обращается в MessageService за результатом запроса на регистрацию, используя 
полученный ранее Id. Если все ок, то создает нового пользователя, сохраняет его в БД, обращаясь в репозиторий, 
а также направляет в сообщение с подтверждением пользователю на Email, используя SendMailer.
* В случае отсутствия ошибок, контроллер возвращает {success: true}, со статусом Ok. Если ошибки были, то 
{success: false, [error1, error2, ...]}, со статусом BadRequest.
* На случай недоступности служб MessagingService и OuterCheckingService или исключений, имеется таймаут ожидания, 
по истечении времени, отказ в регистрации и вывод соответствующего сообщения об ошибке.


### Исходное задание:
Форма регистрации с отправкой имейла после одобрения из внешней системы.
Дана форма регистрации в нашем приложении, в которой необходимо заполнить:

- логин,
- пароль,
- адрес электронной почты,
- ФИО.

После отправки формы, мы регистрируем данные из нее в нашей БД, а также отправляем ее 
для одобрения во внешней системе. Пусть обмен с этой внешней системой будет через некое 
messaging решение. После одобрения или отклонения заявки, наше приложение должно отправить 
сообщение на электронную почту нашему пользователю с результатом проверки.

Стэк: JavaSE 8+, Spring boot 2, dbms - h2. Для тестов предпочтение Junit/Mockito/Assertj, 
т.к. на проекте будут именно они. Остальное по вкусу.

В качестве абстракции над шиной предлагаем взять такой набросок: 
https://pastebin.com/qWjRPuyp

Возвращать из примеров в наброске можно заглушки, дабы сэкономить время на реализацию 
тестового задания. Неплохо при этом помнить, что в реальной эксплуатации любая часть нашей 
системы может отказать. Будем очень рады обоснованиям принятых архитектурных решений. 
Комментарии в коде к ним крайне приветствуются.
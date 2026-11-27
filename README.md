

## What was done:

1. Fetch top 30 US stocks from https://tradernet.com/tradernet-api/quotes-get-top-securities
2. Display logo of a company for ticker using https://tradernet.com/logos/get-logo-by-ticker?ticker= + lowercased ticker
3. Subscribe for quotes using websocket wss.tradernet.com
4. Flash a background for the last updated pcp for 1 sec, similar to how it's done in tradernet app
5. Empty state while subscribing to websocket, similar to tradernet app
6. Error handling is only implemented for fetching top 30 stocks, not websocket connection errors
7. The code is split into several modules, it's just to show a separation of concerns and should be treated as a direction for the subsequent development should it take place, not a real world approach
8. Updates to the list in compose are optimised a bit to account for frequent UI updates from websocket events:
   We create a MutableState for each element in the list, so that each update only affect one element in the list and we don't create a new list each time an event is processed.
   Look [here](ui/src/main/java/com/test/tickersdemo/ui/TickerUiModel.kt) and [here](https://github.com/shuhart/TickersDemo/blob/5ced938d37c94f4b2b32f286f4a5ce4344b99927/ui/src/main/java/com/test/tickersdemo/ui/TickersScreen.kt#L78) for the implementation

<img src="screen_record.gif" width="50%" alt="App Demo">

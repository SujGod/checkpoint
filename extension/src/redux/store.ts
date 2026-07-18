import { configureStore } from "@reduxjs/toolkit";
import { claimsiftApi } from "../api/claimsiftApi";

export const store = configureStore({
  reducer: {
    [claimsiftApi.reducerPath]: claimsiftApi.reducer,
  },

  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware().concat(claimsiftApi.middleware),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

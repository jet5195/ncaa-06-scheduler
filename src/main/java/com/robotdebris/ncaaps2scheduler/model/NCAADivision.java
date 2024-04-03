package com.robotdebris.ncaaps2scheduler.model;

public enum NCAADivision {
	FBS {
		@Override
		public boolean isFBS() {
			return true;
		}
	},
	FCS {
		@Override
		public boolean isFBS() {
			return false;
		}
	},
	FANTASY {
		@Override
		public boolean isFBS() {
			return false;
		}
	};

	public abstract boolean isFBS();
}

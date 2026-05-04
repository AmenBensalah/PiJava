from __future__ import annotations

"""
Compatibility entrypoint.

The pipeline trains and predicts in `train_user_performance.py`.
This script exists so teammates can call a dedicated prediction command.
"""

from src.train_user_performance import main


if __name__ == "__main__":
    main()
